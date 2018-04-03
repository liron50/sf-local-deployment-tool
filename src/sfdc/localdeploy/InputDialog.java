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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;

//Simple dialog tool to get user input
public class InputDialog extends JDialog {
	
	private Map<String, JTextField> m_inputJTextField;
	private Map<String, JTextField> m_inputJPasswordField;
	private Map<String, JComboBox<String>>	m_inputJComboboxField;
	
	public InputDialog(
		   JFrame parent,
		   String[] inputsArr, 
		   String[] labelsArr,
		   Map<String, String[]> m_inputComboValues,
		   String closeButtonName) {
	   
      super(parent);
      
      setModal(true);
      
      setLayout(new BorderLayout());
      
      this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      
      JButton saveButton = new JButton(closeButtonName);
      saveButton.addActionListener(new ActionListener() {
    			  	public void actionPerformed(ActionEvent arg0) {
    			  		close();            
    			  	}
    		  	});

      
      JPanel inputPanel = new JPanel();
      inputPanel.setLayout(new GridLayout(inputsArr.length, 2));
      
      m_inputJTextField = new HashMap<>();
      m_inputJPasswordField = new HashMap<>();
      m_inputJComboboxField = new HashMap<>();
      
      for(int index =0 ; index < inputsArr.length; index++){
    	  
    	  if(inputsArr[index] != null && inputsArr[index].equals("encrypt")){
    		  
    		  m_inputJPasswordField.put(labelsArr[index], new JPasswordField(50));
    		  
    		  inputPanel.add(new JLabel(labelsArr[index]));
        	  inputPanel.add(m_inputJPasswordField.get(labelsArr[index]));
    	  }
    	  else if(inputsArr[index] != null && m_inputComboValues != null && m_inputComboValues.containsKey(labelsArr[index])){
    		  
    		  System.out.println("###m_inputComboValues:OK" );
    		  
    		  m_inputJComboboxField.put(labelsArr[index], new JComboBox<String>(m_inputComboValues.get(labelsArr[index])));
    		  
    		  inputPanel.add(new JLabel(labelsArr[index]));
    		  inputPanel.add(m_inputJComboboxField.get(labelsArr[index]));
    	  }
    	  else{
    		  m_inputJTextField.put(labelsArr[index], new JTextField(inputsArr[index]));
    		  
    		  inputPanel.add(new JLabel(labelsArr[index]));
        	  inputPanel.add(m_inputJTextField.get(labelsArr[index]));
    	  }
      }
      
      
      add(inputPanel, BorderLayout.CENTER);
      
      add(saveButton, BorderLayout.SOUTH);
      
      this.pack();
   }
   
   private void close(){   
	   
	   this.dispose(); 
   }
   
   
   public String getInputValue(String input){
	   
	   return m_inputJTextField.get(input) != null ? m_inputJTextField.get(input).getText() : null;
   }
   
   public String getPassword(String input){
	   
	   return m_inputJPasswordField.get(input) !=null ? m_inputJPasswordField.get(input).getText() : null;
   }
   
   public String getComboboxValue(String input){
	   return m_inputJComboboxField.get(input) !=null ? (String) m_inputJComboboxField.get(input).getSelectedItem() : null;
   }

}