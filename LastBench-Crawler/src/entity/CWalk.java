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

package entity;

public class CWalk {
	
	public CWalk(){
		
		String walk_info_table = null, walk_steps = null;
		
		walk_info_table  = "CREATE TABLE walk_info (walk_id INT(25) NOT NULL AUTO_INCREMENT"
				   + ",startnode VARCHAR(40)"
				   + ",strategy VARCHAR(40)"
				   + ",status INT(2)"
				   + ", ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
				   + ", PRIMARY KEY (walk_id)); ";
		
		walk_steps = "CREATE TABLE walk_steps (walk_id INT(25) NOT NULL"
				   + ",visited_user VARCHAR(40)"
				   + ",step INT(4)"
				   + ", start_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
				   + ", end_ts TIMESTAMP"
				   + ", status INT(2)"
				   + ", PRIMARY KEY (walk_id)); ";
		
		db.SQL.exe_insert(walk_info_table);
		db.SQL.exe_insert(walk_steps);
		
	}
}
