package sfdc.localdeploy;

import javax.swing.SwingWorker;

public class SyncWorker extends SwingWorker<String, Object>{
	
	private String zipFileName;
	
	private String finalResMsg;
	
	public SyncWorker(String zipFileName){
		
		this.zipFileName = zipFileName;
	}
	
	@Override
	public String doInBackground() throws Exception {
		finalResMsg = DeploymentUtilities.deployZip(zipFileName); 
		
		return finalResMsg;
	}
	
	public String getFinalResMsg(){
		return finalResMsg;
	}

}
