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

import runner.HF;

public class CGroup extends CCommon{

	final static String tableName = "groups";
	final static String cParams = "size varchar(30)";

	public int id;
	public String name = null, membersSize = null;

	public CGroup() {
		int_table(tableName, cParams);
	}

	/**
	 * Special case - return object id,
	 * Because there is no relationships related to this object
	 **/
	public static int add_new(String gName, String memSize) {

		boolean db_exists = false;
		
		int _id = -1;
		
		if ( gName != null ){

			gName = gName.replace("\\", "___");
			
			gName = db.Lib.clean(gName);
							
			db_exists = db.Lib.check_exsits(tableName, gName);
			
			// INSERT NEW RECORD
			if (!db_exists){
				
				//HF.print(String.format("----> %s :: %s; IS NEW TO DB", tableName, gName));
				
				_id = db.Lib.insert(tableName, "name, size", String.format("'%s','%s'", gName, memSize));
				
				//HF.print("*** NEWELY INSERTED RECORD IS :: " + _id);
				
			}
			
		}
		else {
			//HF.print(String.format("----> %s :: %s; !!! ALREADY EXISTS  !!! IN DB", tableName, gName));
		}
		
		return _id;

	}
	
}
