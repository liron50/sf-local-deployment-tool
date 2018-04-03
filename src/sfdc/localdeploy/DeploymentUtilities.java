/*
	MIT License
	
	Copyright (c) [year] [fullname]
	
	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
*/

package sfdc.localdeploy;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.CodeCoverageWarning;
import com.sforce.soap.metadata.DeployDetails;
import com.sforce.soap.metadata.DeployMessage;
import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.DeployStatus;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.RunTestFailure;
import com.sforce.soap.metadata.RunTestsResult;
import com.sforce.soap.metadata.TestLevel;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class DeploymentUtilities {

	public static void login() throws ConnectionException{
		
		String username = DeploymentParams.properties.getProperty("SF_USER");
        String password = DeploymentParams.properties.getProperty("SF_PASS"); 
        String URL = "https://" + DeploymentParams.properties.getProperty("TARGET_URL") + ".salesforce.com/services/Soap/u/39.0";
        
        ConnectorConfig config = new ConnectorConfig();
        config.setAuthEndpoint(URL);
        config.setServiceEndpoint(URL);
        config.setManualLogin(true);
        
        
        LoginResult loginResult =  (new PartnerConnection(config)).login(username, password);
        
        ConnectorConfig config2 = new ConnectorConfig();
        
        config2.setServiceEndpoint(loginResult.getMetadataServerUrl());
        config2.setSessionId(loginResult.getSessionId());
        
    	DeploymentParams.metadataConnection = new MetadataConnection(config2);
    }
	
	//Create zip file from list of components
	public static String createZIP(ArrayList<String> files, String zipFileName){
	    	
		String retMsg = "";
	    	
    	//Map of SF type to it's components name will be used to build the package.xml file
    	Map<String, ArrayList<String>> m_type_childs = new HashMap<String, ArrayList<String>>();
	    	
        //First loop get all relevant types and arrange in map
    	for(String fileURL : files){
	    		
    		System.out.println("#### " + fileURL);
	    		
    		String fileFolder = fileURL.substring(0, fileURL.indexOf("\\"));
	    		
    		String fileName = fileURL.substring(fileURL.lastIndexOf("\\") + 1, fileURL.lastIndexOf(".")); 
	    		
    		String sfType = DeploymentParams.folder_SFType_Map.get(fileFolder).typeName;
	    		
	    		//If it is meta-xml file should continue, as it not needed in the pacakge.xml file
	    	if(DeploymentParams.folder_SFType_Map.get(fileFolder).containsMetaXML && (fileURL.endsWith("meta.xml"))){
	    		continue;
	    	}
	    		
			if(! m_type_childs.containsKey(sfType)){
				m_type_childs.put(sfType, new ArrayList<>());
			}
	    	m_type_childs.get(sfType).add(fileName);
	    }
	    	
	    //Build XML file
    	String xmlComponentTypes = "";
	        
    	for(String type : m_type_childs.keySet()){
    		String membersContent = "";
	        	
    		for(String component : m_type_childs.get(type)){
    			membersContent += "<members>" + component + "</members>";
    		}
	        	
    		xmlComponentTypes += "<types>" + membersContent + "<name>" + type + "</name></types>";
    	}
	        
	    	
    	String xmlFull ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
    			"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">"+
    			xmlComponentTypes +
    			"<version>" + DeploymentParams.properties.getProperty("PACAKGE_API") + ".0</version>"+
    			"</Package>";
	    	
    	
    	//Build ZIP File
    	byte[] buffer = new byte[4096];

    	try {

    		FileOutputStream fos = new FileOutputStream(DeploymentParams.properties.getProperty("ZIP_FILES_FOLDER") + "\\" + zipFileName);
    		BufferedOutputStream bos = new BufferedOutputStream(fos);
    		ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);	//VERY IMPORTANT TO USE ZIPARCHIVE library otherwise you might get failure during deployment
    		zos.setMethod(ZipArchiveOutputStream.DEFLATED);
    		zos.setLevel(0);
	            
    		FileInputStream inPacakge = null;
		     
    		//Add the pacakge.xml file
	        File pacakgeXMLFile = new File(DeploymentParams.MANIFEST_FILE);
			FileWriter fw = new FileWriter(pacakgeXMLFile);
        	fw.write(xmlFull);
        	fw.close();
	        	
        	ZipArchiveEntry zePackage = new ZipArchiveEntry("unpackaged\\" + DeploymentParams.MANIFEST_FILE);
        	zos.putArchiveEntry(zePackage);
        	inPacakge = new FileInputStream(DeploymentParams.MANIFEST_FILE);
		    
        	int len1;
        	while ((len1 = inPacakge .read(buffer)) > 0) {
        		zos.write(buffer, 0, len1);
        	}
        	inPacakge.close();


        	//Add all other files to the zip
        	for (String fileURL : files) {
	            	
        		String fileFolder = fileURL.substring(0, fileURL.indexOf("\\"));
        		String fileName = fileURL.substring(fileURL.lastIndexOf("\\") + 1); 
	            	
        		ZipArchiveEntry ze = new ZipArchiveEntry("unpackaged\\" + fileFolder + File.separator + fileName);
        		zos.putArchiveEntry(ze);
        		FileInputStream in = new FileInputStream(DeploymentParams.properties.getProperty("LOCAL_FOLDER") + File.separator + fileURL);

        		int len;
        		while ((len = in.read(buffer)) > 0) {
        			zos.write(buffer, 0, len);
        		}
        		in.close();
        		zos.closeArchiveEntry();
        	}

        	zos.close();

    	} catch (IOException ex) {
    		ex.printStackTrace();
    		retMsg = ex.getMessage();
    	}
	        
    	return retMsg;
	}
	
	//Process the deployment
	public static String deployZip(String zipFileName) throws Exception{
    	
    	String finalRetMsg = "";
    	    	
    	progressMessages.add("Deployment started for zip: " + zipFileName);
        
    	//Get ZIP file content
    	byte[] zipByets = null;

    	File zipFile = new File(DeploymentParams.properties.getProperty("ZIP_FILES_FOLDER") + "\\" + zipFileName);
        if (!zipFile.exists() || !zipFile.isFile()) {
            throw new Exception("Cannot find the zip file for deploy on path:" + zipFile.getAbsolutePath());
        }

        FileInputStream fileInputStream = new FileInputStream(zipFile);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead = 0;
            while (-1 != (bytesRead = fileInputStream.read(buffer))) {
                bos.write(buffer, 0, bytesRead);
            }
            zipByets = bos.toByteArray();
            
        } finally {
            fileInputStream.close();
        }
        
        //Create deploy options
        Boolean isRealDeploy = DeploymentParams.properties.getProperty("DEPLOT_METHOD").equals("Deploy");	//Deploy or only Validate
        
    	DeployOptions deployOptions = new DeployOptions();
    	deployOptions.setPerformRetrieve(false);
    	deployOptions.setRollbackOnError(true);
    	deployOptions.setTestLevel(TestLevel.RunLocalTests);
    	deployOptions.setCheckOnly(!isRealDeploy);
    	
    	//Start deployment
    	AsyncResult asyncResult = DeploymentParams.metadataConnection.deploy(zipByets, deployOptions);
    	
    	int checkCount = 0;
    	DeployResult deployResult = null;

    	try{
	    	do{
	    		Thread.sleep(DeploymentParams.DEPLOY_CHECK_INTERVAL * (checkCount + 1));
	
	    		if(checkCount++ > DeploymentParams.MAX_NUM_POLL_REQUESTS) {
	    			progressMessages.add("Request timed out. If this is a large set of metadata components, check that the time allowed by MAX_NUM_POLL_REQUESTS is sufficient.");
	    			
	    			throw new Exception("Request timed out. If this is a large set of metadata components, check that the time allowed by MAX_NUM_POLL_REQUESTS is sufficient.");
	    		}
	
	    		deployResult = DeploymentParams.metadataConnection.checkDeployStatus(asyncResult.getId(), true);
	    		
	    		DeployStatus status = deployResult.getStatus();
	    		
	    		if(status == DeployStatus.Pending){
	    			progressMessages.add("Status is Pending.");
	    		}
	    		else if(status == DeployStatus.InProgress){
	    			if(deployResult.getNumberTestsTotal() > 0){
	    				progressMessages.add("Status is In Progress. Test run: " + deployResult.getNumberTestsCompleted() + " from " + deployResult.getNumberTestsTotal() 
	    					+ (deployResult.getNumberTestErrors() > 0 ? "(" + deployResult.getNumberTestErrors() + " Failed)" : ""));
	    			}
	    			else{
	    				progressMessages.add("Status is In Progress. Components: " + deployResult.getNumberComponentsDeployed() + " from " + deployResult.getNumberComponentsTotal()
	    					+ (deployResult.getNumberComponentErrors() > 0 ? "(" + deployResult.getNumberComponentErrors() + " Failed)" : ""));
	    			}
	    		}
	    		else{
	    			progressMessages.add("Status is: " + status);
	    		}
	    		
	    		if (!deployResult.isDone() && checkCount % 3 == 0) {
	    			printErrors(deployResult, "Failures for deployment in progress:\n");
	    		}
	
	    	}
	    	while (!deployResult.isDone());
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		
    		progressMessages.add(e.getMessage());
    	}
    	finally {
    		deploymentStatus = "complete";
    		
    		if (!deployResult.isSuccess()) {
    			finalRetMsg = "The files were not successfully deployed.\n";
        		
                printErrors(deployResult, "Final list of failures:\n");
            }
        	else{
        		finalRetMsg = "The file " + zipFileName + " was successfully deployed\n";
        	}
    		
		}
    	
    	return finalRetMsg;
    }
	
	//contains the statuses of the deployments
	private static String deploymentStatus;;
	
	//contains list of progress messages for the deployment
	private static ArrayList<String> progressMessages = new ArrayList<String>();
	
	public static String getDeploymentStatus(){
		return deploymentStatus != null ? deploymentStatus : "";
	}
	
	public static ArrayList<String> getProgressMessage(){
		return progressMessages;
	}
	
	//Get deployment errors
	private static void printErrors(DeployResult result, String messageHeader) {
        DeployDetails details = result.getDetails();
        StringBuilder stringBuilder = new StringBuilder();
        if (details != null) {
            DeployMessage[] componentFailures = details.getComponentFailures();
            for (DeployMessage failure : componentFailures) {
                String loc = "(" + failure.getLineNumber() + ", " + failure.getColumnNumber();
                if (loc.length() == 0 && !failure.getFileName().equals(failure.getFullName()))
                {
                    loc = "(" + failure.getFullName() + ")";
                }
                stringBuilder.append(failure.getFileName() + loc + ":"  + failure.getProblem()).append('\n');
            }
            RunTestsResult rtr = details.getRunTestResult();
            if (rtr.getFailures() != null) {
                for (RunTestFailure failure : rtr.getFailures()) {
                    String n = (failure.getNamespace() == null ? "" : (failure.getNamespace() + ".")) + failure.getName();
                    stringBuilder.append("Test failure, method: " + n + "." + failure.getMethodName() + " -- " + failure.getMessage() + " stack " + failure.getStackTrace() + "\n\n");
                }
            }
            if (rtr.getCodeCoverageWarnings() != null) {
                for (CodeCoverageWarning ccw : rtr.getCodeCoverageWarnings()) {
                    stringBuilder.append("Code coverage issue");
                    if (ccw.getName() != null) {
                        String n = (ccw.getNamespace() == null ? "" : (ccw.getNamespace() + ".")) + ccw.getName(); 
                        stringBuilder.append(", class: " + n);
                    }
                    stringBuilder.append(" -- " + ccw.getMessage() + "\n");
                }
            }
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.insert(0, messageHeader);
            System.out.println(stringBuilder.toString());
            progressMessages.add(stringBuilder.toString());
        }
    }
}
