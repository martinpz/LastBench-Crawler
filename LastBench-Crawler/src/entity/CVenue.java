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
import de.umass.lastfm.Venue;

public class CVenue extends CCommon{

	final static String tableName = "venue";
	final static String cParams = "country varchar(255), address varchar(255), geo varchar(255)";

	public CVenue() {
		int_table(tableName, cParams);
	}
	
	public static int add_new(Venue v){
		
		boolean db_exists = false;
		
		int _id = -1;
		
		if ( v != null ){
			
			String 	vName = v.getName();
			
			vName = vName.replace("\\", "___");
			
			vName = db.Lib.clean(vName);
							
			db_exists = db.Lib.check_exsits(tableName, vName);
			
			// INSERT NEW RECORD
			if (!db_exists){
				
				//HF.print(String.format("----> %s :: %s; IS NEW TO DB", tableName, vName));
				
				String _address = v.getStreet() + "-" + v.getPostal();
				String geo = v.getLatitude() + "-" + v.getLongitude();
				
				String _rows   = "name, country, address, geo";
				String _values = "'%s', '%s', '%s', '%s'";
				
				_values = String.format(_values, v.getName(), v.getUrl(), v.getCountry(), _address, geo);
				
				_id = db.Lib.insert(tableName, _rows, _values);
				
				//HF.print("*** NEWELY INSERTED RECORD IS :: " + _id);
				
			}
			
			else {
				//HF.print(String.format("----> %s :: %s; !!! ALREADY EXISTS  !!! IN DB", tableName, vName));			
			}
		}
		
		v = null;

		return _id;	
	}
	
}
