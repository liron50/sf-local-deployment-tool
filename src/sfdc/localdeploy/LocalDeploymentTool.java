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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.Timer;

public class LocalDeploymentTool extends JFrame{

	private JToolBar toolBar;
	
	private JMenuItem optionsItem;
	private JMenuItem closeItem;
	
	private JButton addFilesButton;
	private JButton clearFilesButton;
	private JButton createZIPButton;
	private JButton loadZipButton;
	private JButton deployZIPButton;
	
	private JList<String> deployCompList;
	
	private JTextArea msgTextArea;
	
	private DefaultListModel<String> deployCompModelList;
	
	private String zipFileName;
	
	//Used to check result during deployment
	public Timer timer;
	
	public LocalDeploymentTool(){
		try{
		
			setTitle("Deployment Tool");
			
			setLayout(new BorderLayout());
			
			FileInputStream input = new FileInputStream(new File("DeploymentTool.properties"));
	        
			DeploymentParams.properties.load(input);
			
			//Create menu bar
			JMenuBar menuBar = new JMenuBar();
			
			JMenu optionMenu = new JMenu("Options");
			
			optionsItem = new JMenuItem(new OptionsAction());
			closeItem = new JMenuItem(new CloseAction());
			
			optionMenu.add(optionsItem);
			optionMenu.add(closeItem);
			
			menuBar.add(optionMenu);
			
			setJMenuBar(menuBar);
			
			
			//Create tool bar with all buttons
			toolBar = new JToolBar();
			
			addFilesButton = new JButton(new AddFilesAction());
			clearFilesButton = new JButton(new ClearFilesAction());
			createZIPButton = new JButton(new CreateDeployPackageAction());
			loadZipButton = new JButton(new LoadZipAction());
			deployZIPButton = new JButton(new DeployZIPAction());
			
			toolBar.add(addFilesButton);
			toolBar.add(clearFilesButton);
			toolBar.add(createZIPButton);
			toolBar.add(loadZipButton);
			toolBar.add(deployZIPButton);
			
			//Create area for list of components
			deployCompModelList = new DefaultListModel<>();
			
			deployCompList = new JList<String>();
			deployCompList.setBackground(Color.BLACK);
			deployCompList.setForeground(Color.WHITE);
			
			//Create area for messages during deployment
			msgTextArea = new JTextArea();
			msgTextArea.setEditable(false);
			msgTextArea.setBackground(Color.BLACK);
			msgTextArea.setForeground(new Color(30, 175, 46));
			msgTextArea.setFont(new Font ("Arial", Font.BOLD, 12));
			
			
			getContentPane().add(toolBar, BorderLayout.NORTH);
			getContentPane().add(new JScrollPane(deployCompList), BorderLayout.SOUTH);
			getContentPane().add(new JScrollPane(msgTextArea), BorderLayout.CENTER);
			
			//Event when closing the app
			addWindowListener(new WindowAdapter()
			{
			    public void windowClosing(WindowEvent e)
			    {
			    	try{
			    		closeWindow();
			    	}
			    	catch(Exception ex){
			    		ex.printStackTrace();
						
						JOptionPane.showMessageDialog(LocalDeploymentTool.this, "Error: " + ex.getMessage());
			    	}
			    }
			});
			
			deployZIPButton.setEnabled(false);
			
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setSize(800, 800);
			setVisible(true);
		}
		catch(Exception ex){
			ex.printStackTrace();
		
			JOptionPane.showMessageDialog(LocalDeploymentTool.this, "Error: " + ex.getMessage());
		}
	}
	
	public void closeWindow() throws FileNotFoundException, IOException{
		
		DeploymentParams.properties.setProperty("SF_PASS", "");
		
		DeploymentParams.properties.store(new FileOutputStream("DeploymentTool.properties"), null);
		
		this.dispose();
	}
	
	//Close action for the menu option
	private class CloseAction extends AbstractAction{
		public CloseAction(){
			putValue(Action.NAME, "Close");
			
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			try{
				closeWindow();
			}
			catch(Exception ex){
				ex.printStackTrace();
				
				JOptionPane.showMessageDialog(LocalDeploymentTool.this, "Error: " + ex.getMessage());
			}
		}
	}
	
	//Option action for the menu - allow to modify the property file
	private class OptionsAction extends AbstractAction{
		
		public OptionsAction(){
			putValue(NAME, "Options");
		}
		
		public void actionPerformed(ActionEvent event){
			
			try{
				InputDialog inputDialog = new InputDialog(
						LocalDeploymentTool.this,
							new String[]{DeploymentParams.properties.getProperty("PACAKGE_API"), DeploymentParams.properties.getProperty("DEPLOT_METHOD"), DeploymentParams.properties.getProperty("LOCAL_FOLDER"), DeploymentParams.properties.getProperty("ZIP_FILES_FOLDER")},
							new String[]{"Pacakge API Version", "Deploy/Validate", "Source Folder", "ZIP Files Folder"},
							DeploymentParams.m_ComboValues, 
							"Save");
			
			
				inputDialog.setLocationRelativeTo(LocalDeploymentTool.this);
				inputDialog.setPreferredSize(new Dimension(700, 150));
				inputDialog.pack();
				inputDialog.setVisible(true);
				
				if(inputDialog.getInputValue("Pacakge API Version") != null){
					DeploymentParams.properties.setProperty("PACAKGE_API", inputDialog.getInputValue("Pacakge API Version"));
				}
				if(inputDialog.getComboboxValue("Deploy/Validate") != null){
					DeploymentParams.properties.setProperty("DEPLOT_METHOD", inputDialog.getComboboxValue("Deploy/Validate"));
				}
				if(inputDialog.getInputValue("Source Folder") != null){
					DeploymentParams.properties.setProperty("LOCAL_FOLDER", inputDialog.getInputValue("Source Folder"));
				}
				if(inputDialog.getInputValue("ZIP Files Folder") != null){
					DeploymentParams.properties.setProperty("ZIP_FILES_FOLDER", inputDialog.getInputValue("ZIP Files Folder"));
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
				
				JOptionPane.showMessageDialog(LocalDeploymentTool.this, "Error: " + ex.getMessage());
			}
		}
		
	}
	
	//Choose files from local file system
	private class AddFilesAction extends AbstractAction{
		public AddFilesAction(){
			putValue(Action.NAME, "Add Files");
		}
		
		public void actionPerformed(ActionEvent event){
			JFileChooser chooser = new JFileChooser();
			
			if(DeploymentParams.properties.getProperty("LOCAL_FOLDER").isEmpty()){
				JOptionPane.showMessageDialog(LocalDeploymentTool.this, "Please first Setup Local Folder.");
			}
			else{
			
				chooser.setCurrentDirectory(new File(DeploymentParams.properties.getProperty("LOCAL_FOLDER")));
				chooser.setDialogTitle("Select Files");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setMultiSelectionEnabled(true);
				
				if(chooser.showOpenDialog(LocalDeploymentTool.this) == JFileChooser.APPROVE_OPTION){
					for(File file : chooser.getSelectedFiles()){
						
						String fileAbsolutePath = file.getAbsolutePath();
						
						fileAbsolutePath = fileAbsolutePath.substring(fileAbsolutePath.indexOf("src") + 4);
						
						System.out.println("###FILE : " + fileAbsolutePath);
						
						DeploymentParams.selectedFiles.add(fileAbsolutePath);
						
						appendComponentList(fileAbsolutePath);
						
						deployZIPButton.setEnabled(false);
					}
				}
			}
		}
	}
	
	//Clear the selected list
	private class ClearFilesAction extends AbstractAction{
		
		public ClearFilesAction(){
			putValue(Action.NAME, "Clear Files");
		}
		
		public void actionPerformed(ActionEvent event){
			
			//Clear current content
			DeploymentParams.selectedFiles.clear();
			clearFileJList();
			
			deployZIPButton.setEnabled(false);
		}
	}
	
	//Create zip file from the selected components
	private class CreateDeployPackageAction extends AbstractAction{
		
		public CreateDeployPackageAction(){
			putValue(Action.NAME, "Create Package");
		}
		
		public void actionPerformed(ActionEvent event){
			try{
				if(DeploymentParams.properties.getProperty("ZIP_FILES_FOLDER").isEmpty()){
					JOptionPane.showMessageDialog(LocalDeploymentTool.this, "Please setup folder for zip files.");
					return;
				}
				else if(DeploymentParams.selectedFiles.isEmpty()){
					JOptionPane.showMessageDialog(LocalDeploymentTool.this, "No files were selected.");
					return;
				}
				
				zipFileName = JOptionPane.showInputDialog(LocalDeploymentTool.this, "Setup name for ZIP file");
				
				zipFileName = zipFileName.endsWith(".zip") ? zipFileName : zipFileName + ".zip";
				
				//Create package.xml and ZIP file
				String res = DeploymentUtilities.createZIP(DeploymentParams.selectedFiles, zipFileName);
				
				appendMsgTextArea(res.isEmpty() ? "ZIP file created successfully." : res);
				
				deployZIPButton.setEnabled(true);
				
			}
			catch(Exception ex){
				ex.printStackTrace();
				
				System.out.println("###" + ex.getMessage() + " : " + ex.getStackTrace());
				
				JOptionPane.showMessageDialog(LocalDeploymentTool.this, "Exception: " + ex.getMessage());
			}
		}
	}
	
	//Load existing zip file content
	private class LoadZipAction extends AbstractAction{
		
		public LoadZipAction(){
			putValue(Action.NAME, "Load ZIP");
		}
		
		public void actionPerformed(ActionEvent event){
			
			//Open file chooser. Save the file name in zipFileName
			try {
				
				if(DeploymentParams.properties.getProperty("ZIP_FILES_FOLDER").isEmpty()){
					JOptionPane.showMessageDialog(LocalDeploymentTool.this, "Please setup folder for zip files.");
					return;
				}
					
				JFileChooser chooser = new JFileChooser();
			
				chooser.setCurrentDirectory(new File(DeploymentParams.properties.getProperty("ZIP_FILES_FOLDER")));
				chooser.setDialogTitle("Choose ZIP");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
			
				if(chooser.showOpenDialog(LocalDeploymentTool.this) == JFileChooser.APPROVE_OPTION){

					
					if(!chooser.getSelectedFile().getAbsolutePath().contains(DeploymentParams.properties.getProperty("ZIP_FILES_FOLDER"))){
						JOptionPane.showMessageDialog(LocalDeploymentTool.this, "Invalid ZIP File.");
						return;
					}
					else{
						//Clear current content
						DeploymentParams.selectedFiles.clear();
						clearFileJList();
						
						zipFileName = chooser.getSelectedFile().getName();
						
						//Should read the ZIP content and display the files in JList 
						ZipFile zipFile = new ZipFile(DeploymentParams.properties.getProperty("ZIP_FILES_FOLDER") + File.separator + zipFileName);

					    Enumeration<? extends ZipEntry> entries = zipFile.entries();

					    while(entries.hasMoreElements()){
					        ZipEntry entry = entries.nextElement();
					        
					        if(!entry.getName().contains("package.xml")){
					        
					        	String filePath = entry.getName();
					        	
					        	filePath = filePath.substring(filePath.indexOf("/") + 1);
					        	
					        	DeploymentParams.selectedFiles.add(filePath.replaceAll("/", "\\\\"));
					        
					        	appendComponentList(filePath.replaceAll("/", "\\\\"));
					        }
					    }
					    zipFile.close();
					    
						deployZIPButton.setEnabled(true);
					}
				}
			} 
			catch (Exception ex) {
				
				ex.printStackTrace();
				
				JOptionPane.showMessageDialog(LocalDeploymentTool.this, "Error: " + ex.getMessage());
			}
		}
	}
	
	//Deploy the zip file
	private class DeployZIPAction extends AbstractAction{
		
		public DeployZIPAction(){
			putValue(Action.NAME, "Deploy ZIP");
		}
		
		public void actionPerformed(ActionEvent event){
			try {
				
				//Should choose the target environment, user/password. Might take it from the property if it's there.
				InputDialog inputDialog = new InputDialog(
						LocalDeploymentTool.this,
							new String[]{DeploymentParams.properties.getProperty("SF_USER"), "encrypt", DeploymentParams.properties.getProperty("TARGET_URL"), DeploymentParams.properties.getProperty("DEPLOT_METHOD")},
							new String[]{"User Name", "Password", "Destination", "Deploy/Validate"}, 
							DeploymentParams.m_ComboValues, 
							"Start");
				
				
				inputDialog.setLocationRelativeTo(LocalDeploymentTool.this);
				inputDialog.setPreferredSize(new Dimension(500, 150));
				inputDialog.pack();
				inputDialog.setVisible(true);
				
				//Validate user input
				if(!inputDialog.getInputValue("User Name").isEmpty()){
					DeploymentParams.properties.setProperty("SF_USER", inputDialog.getInputValue("User Name"));
				}
				else{
					JOptionPane.showMessageDialog(LocalDeploymentTool.this, "Missing user name.");
					return;
				}
				if(!inputDialog.getPassword("Password").isEmpty()){
					DeploymentParams.properties.setProperty("SF_PASS", inputDialog.getPassword("Password"));
				}
				else{
					JOptionPane.showMessageDialog(LocalDeploymentTool.this, "Missing password.");
					return;
				}
				if(inputDialog.getInputValue("Destination") != null){
					DeploymentParams.properties.setProperty("TARGET_URL", inputDialog.getInputValue("Destination"));
				}
				if(inputDialog.getComboboxValue("Deploy/Validate") != null){
					DeploymentParams.properties.setProperty("DEPLOT_METHOD", inputDialog.getComboboxValue("Deploy/Validate"));
				}
				
				
				DeploymentUtilities.login();
				
				
				if(JOptionPane.showConfirmDialog(LocalDeploymentTool.this, "Continue with deployment?") == JOptionPane.OK_OPTION){
				
					//Show first message with details regarding deployment
					appendMsgTextArea(
							"Starting deployment userName: " + inputDialog.getInputValue("User Name") + 
							", org: " + inputDialog.getInputValue("Destination") + 
							", method: " + inputDialog.getComboboxValue("Deploy/Validate") + "\n");
					
					SyncWorker syncWorker = new SyncWorker(zipFileName);
					syncWorker.execute();
					
					timer = new Timer(1000, new ActionListener() {
				        @Override
				        public void actionPerformed(ActionEvent e) {
				        	
				        	if(! DeploymentUtilities.getProgressMessage().isEmpty()){
				        		
				        		appendMsgTextArea(DeploymentUtilities.getProgressMessage().remove(0));
				        	}
				        	else if(DeploymentUtilities.getDeploymentStatus().equals("complete")){
				        		
				        		appendMsgTextArea(syncWorker.getFinalResMsg());
				            
				        		timer.stop();
				        	}
				        }
				    });
					timer.start();
				}
			} 
			catch (Exception ex) {
				
				ex.printStackTrace();
				
				JOptionPane.showMessageDialog(LocalDeploymentTool.this, "Error: " + ex.getMessage());
			}
		}
	}
	
	//Add components to the list on the screen
	private void appendComponentList(String msg){
		
		if(!deployCompModelList.contains(msg)){
			deployCompModelList.addElement(msg);
		
			deployCompList.setModel(deployCompModelList);
		}
	}

	//Clear components list from the screen
	private void clearFileJList(){
		deployCompModelList.clear();
		
		deployCompList.setModel(deployCompModelList);
	}
	
	//Add message to the message area
	private void appendMsgTextArea(String msg){
		msgTextArea.append(msg + "\n");
	}
	
	public static void main(String[] args) {
		
		new LocalDeploymentTool();

	}
}
