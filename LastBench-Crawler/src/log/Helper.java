/*
* Copyright (C) 2014 University of Freiburg.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import runner.HF;

/**
 * Helper function used by the logger 
 * 
 * @version: 0.1
 ***/
public class Helper {
	
	//Appending new line to a give text file path
	public static void append_to_file (String fileLoc, String content){
		
		Writer output;
		
		String msgTemplate = "", msg = "";
		
		//msgTemplate = "%s::%s\n";
		
		//msg         = String.format(msgTemplate, get_current_time(), content); //printing timestamp of log
		
		msg = content;
		
		try {
			
			output = new BufferedWriter(new FileWriter(fileLoc, true));
			output.append(msg);
			output.close();
			
		} catch (IOException e) {
			HF.print("!! ERROR @ JDBC" + e);
			log.System.add_to_log("!!ERROR!!" + e);
		}
	
	}
	
	public static String get_current_time(){
		
		String timeStamp = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		
		return timeStamp;
	}


}
