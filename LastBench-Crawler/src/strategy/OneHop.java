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

package strategy;

import runner.HF;
import de.umass.lastfm.Caller;
import entity.*;

public class OneHop {

	static String strategyName = "One-Hop-Walk";
	static String sNode = "Jeandache";
	static int walk_id = 0;
	
	final static int crawling_lvl = 1;

	public static void int_walk(){

		Caller.getInstance().setDebugMode(false);
		
		HF.print("############");
		HF.print("Time stamp: " + log.Helper.get_current_time());
		HF.print("Logging threshold: " + HF.threshold + " Sec");
		HF.print(String.format("## Strategy: %s", strategyName));
		
		long begin = System.currentTimeMillis();
			
		Api_Catcher.firstRun = true;
		
		String _sql_inert_walk_info = "insert into walk_info (startnode, strategy, status) values ('%s', '%s', '%s')";
		
		_sql_inert_walk_info = String.format(_sql_inert_walk_info, sNode, strategyName, 0);
		
		walk_id = db.SQL.exe_insert(_sql_inert_walk_info);
		
		try{}finally{
			CUser.add_new(sNode, crawling_lvl);			
		}

		
		HF.print( "###" + HF.get_cost_pure(strategyName, begin));
		HF.print("############");
		
	}
	
	public static void main(String args[]) throws Exception {

		String _sql_update_walk_info = "UPDATE walk_info SET status = %s WHERE walk_id = %s";
		
		try {
			
			int_walk();
			
		} catch (Exception e) {
			
			log.System.add_to_log("ERROR CTACHED AT MAIN" + e);
			
			_sql_update_walk_info = String.format( _sql_update_walk_info , 3, walk_id);
			 db.SQL.exe_insert(_sql_update_walk_info);	 

		}
		
		_sql_update_walk_info = String.format( _sql_update_walk_info , 2, walk_id);
		 db.SQL.exe_insert(_sql_update_walk_info);
		 
	}
}
