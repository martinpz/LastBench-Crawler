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

import de.umass.lastfm.Event;
import runner.HF;

public class CEvent extends CCommon{

	final static String tableName = "event";
	final static String cParams   = "title varchar(250), headlinerId varchar(250), venueId int(20)"
								  + ", startDate varchar(255), attendance int(10), reviews int(10), website varchar(50)";

	int id;
	public String name = null, url = null;

	public CEvent() {
		int_table(tableName, cParams);
	}
	
	public static int add_new(Event e){
		
		boolean db_exists = false;
		
		int _id = -1;
		
		if ( e != null ){

			String eName = e.getTitle();
			
//			eName = eName.replace("\\", "___");
			
			eName = db.Lib.clean(eName);
							
			db_exists = db.Lib.check_exsits(tableName, eName);
			
			// INSERT NEW RECORD
			
			if (!db_exists){
				
				//HF.print(String.format("----> %s :: %s; IS NEW TO DB", tableName, eName));
				
				_id = db.Lib.simpleInsert(tableName, eName);
				
				try{}
				finally{
					int _vId = CVenue.add_new(e.getVenue());
					
					if (_vId != -1){
						fill_all_info(e, _vId);
						db.Lib.update_to_completed(tableName, eName);
					}
				}
				//HF.print("*** NEWELY INSERTED RECORD IS :: " + _id);
				
			}
			else {
				//HF.print(String.format("----> %s :: %s; !!! ALREADY EXISTS  !!! IN DB", tableName, eName));			
			}
		}
		
		e = null;
		
		return _id;
	}

	private static void fill_all_info(Event e, int venueId) {
		
		String _tmp = "title = '%s', headlinerId = '%s', venueId = '%s', startDate = '%s', attendance = '%s', reviews = '%s', website = '%s'";
		
		_tmp = String.format(_tmp, db.Lib.clean(e.getTitle()), db.Lib.clean(e.getHeadliner()), Integer.toString(venueId), e.getStartDate(), e.getAttendance(), e.getReviews(), e.getWebsite());

		db.Lib.update(tableName, _tmp, e.getTitle());
		
		e = null;
		
	}

}
