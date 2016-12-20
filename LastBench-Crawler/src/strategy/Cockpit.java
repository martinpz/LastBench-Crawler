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

public class Cockpit {

	public static void soft_stop(int walk_id){
		db.SQL.exe_insert(String.format("UPDATE walk_info SET status = 4 WHERE walk_id = %s", walk_id));
		
		HF.print(String.format(" **** !! ATTENTION !! WALK_ID: %s; requested to be paused", walk_id));
	}
	
	public static void _continue(String strageyName, String uName, int crawling_lvl, int walk_id){
		
		int _walkStatus = db.SQL.exe_select(String.format("SELECT status FROM walk_info WHERE walk_id = %s", walk_id));

		if(_walkStatus == -1){
			HF.print(String.format(" **** !! ERROR !! WALK_ID: %s; requested to be resumed, doesn't exisits our database", walk_id));	
		}
		
		if(_walkStatus == 2){
			HF.print(String.format(" **** !! ATTENTION !! WALK_ID: %s; requested to be resumed have already been finished", walk_id));
		}
		
		if (_walkStatus != 2 && _walkStatus != -1){

			try {
				HF.print(String.format(" **** WALK_ID: %s; requested to be resumed", walk_id));				
			}
			finally{
				MRHW _obj = new MRHW(strageyName, uName, crawling_lvl, walk_id); 						
				_obj = null;
			}
		}
		
	}
	
	public static void main(String args[]){
		
		soft_stop(6);
//		_continue("One-Hop-Walk", "IZ-US", 11, 3);
//		_continue("TWO-Hop-Walk", "XXXX", 22, 2);
		
	}
}
