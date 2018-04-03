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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.sforce.soap.metadata.MetadataConnection;

public class DeploymentParams {

	public static Properties properties = new Properties();
	
	public static ArrayList<String> selectedFiles = new ArrayList<>();
	
	public static final String MANIFEST_FILE = "package.xml";
	
	public static final int DEPLOY_CHECK_INTERVAL = 1000;
	
	// maximum number of attempts to deploy the zip file
    public static final int MAX_NUM_POLL_REQUESTS = 50;
	
	public static MetadataConnection metadataConnection;
	
	//Map between the folder of the type to its SF name
	public static Map<String, SFMetadataType> folder_SFType_Map = new HashMap<String, SFMetadataType>(){{
		put("applications", new SFMetadataType("CustomApplication", false));
        put("approvalProcesses", new SFMetadataType("ApprovalProcess", false));
        put("classes", new SFMetadataType("ApexClass", true));
        put("components", new SFMetadataType("ApexComponent", true));
        put("customMetadata", new SFMetadataType("CustomMetadata", false));
        put("customPermissions", new SFMetadataType("CustomPermission", false));
        put("dashboards", new SFMetadataType("Dashboard", false));
        put("email", new SFMetadataType("EmailTemplate", false));
        put("flows", new SFMetadataType("Flow", false));
        put("groups", new SFMetadataType("Group", false));
        put("labels", new SFMetadataType("CustomLabels", false));
        put("layouts", new SFMetadataType("Layout", false));
        put("objects", new SFMetadataType("CustomObject", false));
        put("objectTranslations", new SFMetadataType("CustomObjectTranslation", false));
        put("pages", new SFMetadataType("ApexPage", true));
        put("permissionsets", new SFMetadataType("PermissionSet", false));
        put("profiles", new SFMetadataType("Profile", false));
        put("queues", new SFMetadataType("Queue", false));
        put("reports", new SFMetadataType("Report", false));
        put("reportTypes", new SFMetadataType("ReportType", false));
        put("roles", new SFMetadataType("Role", false));
        put("staticresources", new SFMetadataType("StaticResource", true));
        put("tabs", new SFMetadataType("CustomTab", false));
        put("translations", new SFMetadataType("Translations", false));
        put("triggers", new SFMetadataType("ApexTrigger", true));
        put("weblinks", new SFMetadataType("Weblink", false));
        put("workflows", new SFMetadataType("Workflow", false));
        
	}};
	
	public static class SFMetadataType{
		public String typeName;
		public boolean containsMetaXML;
		
		public SFMetadataType(String name, boolean containsMeta){
			typeName = name;
			containsMetaXML = containsMeta;
		}
	}
	
	//Selection options during user input
	public static Map<String, String[]> m_ComboValues = new HashMap<String, String[]>(){{
		put("Deploy/Validate", new String[]{"Deploy", "Validate"});
	}};
}
