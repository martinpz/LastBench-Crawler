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

import java.util.Collection;

import runner.HF;
import strategy.Api_Catcher;
import de.umass.lastfm.Album;
import de.umass.lastfm.Tag;

public class CAlbum extends CCommon{

	final static String tableName = "album";
	
	final static String cParams = "url varchar(180), artist varchar(180), albumMbID varchar(180), releasedate varchar(50), listeners int(25), playcount int(25), visisted int(1) DEFAULT 0";
	
	public static int id;
	
	/*** CREATING TABLE AND ITS RELATIONSHIPS ***/
	
	public CAlbum(){
		
		int_table(tableName, cParams);
		int_relations_tables();
	
	}
	
	public void int_relations_tables() {
	
		db.Lib.cRelTable("album_topTags", tableName, "tag");
			db.SQL.exe_insert("ALTER TABLE album_topTags ADD count int (15)");
	
	}
	

	public static void add_new(Album a, int lvl) {
		
		if ( a != null ){
			control_object(a, lvl);
		}
	}

	private static void full_info(Album a) {
			
		String _tmp = "";
		
		_tmp = "url = '%s', artist = '%s', albumMbID = '%s', releasedate = '%s', listeners = '%s', playcount = '%s'";
		
		_tmp = String.format(_tmp, db.Lib.clean(a.getUrl()), db.Lib.clean(a.getArtist()), a.getMbid(), a.getReleaseDate(), Integer.toString(a.getListeners()), Integer.toString(a.getPlaycount()));

		db.Lib.update(tableName, _tmp, a.getName());
		
		a = null;
		
	}

	private static void control_object(Album a, int lvl){
		
		String aName = db.Lib.clean(a.getName());
		
		boolean db_exists = db.Lib.check_exsits(tableName, aName);
		
		if (!db_exists){
			
			id = db.Lib.simpleInsert(tableName, aName);
			
			full_info(a);
			
			if (lvl > 0){
				db.Lib.update_to_seen(tableName, aName);
				fill_relations(id, a, lvl);
				db.Lib.update_to_completed(tableName, aName);
			}
		}
		
		if (db_exists){
			
			id = db.Lib.get_id(tableName, aName);
			
			if ( lvl > 0 ) {
				//#TODO insert other related information
				//#TODO: do some magic here
				//HF.print(String.format("----> %s :: %s; ALREADY EXISTS IN DB", tableName, aName));
			}
			
		}
		
		a = null;
		
	}

	
	/****************** FILL RELATED USER RELATIONSHIP ******************/
	
	private static void fill_relations(int aId, Album a, int lvl) {
		
//		HF.print("###");
//		HF.print(String.format("{%s} id: %s, name: %s;", tableName, aId, a.getName()));
		
		fill_top_tags(aId, a, "album_topTags", 1);
		
		a = null;
	}
	
	/****************** TOP TAGS ******************/
	
	private static void fill_top_tags(int aId, Album a, String listName, int lvl){
		
		try {
			
			long begin;
			
			begin = System.currentTimeMillis();
			
			Collection<Tag> tags = Album.getTopTags(a.getArtist(), a.getMbid(), Config.artist_topTags, getAPI());
			
			HF.get_cost_p(" .. .. API_Call_ALBUM_TOP_TAGS", begin);

			begin = System.currentTimeMillis();
			
			if ( tags.size() > 0 ) {
				
				for (Tag t : tags) {
					
					if (t != null) {
						
						//HF.print(t.getName());
						
						try{
							CTag.add_new(t, 0);
						}
						catch(Exception e){
							HF.print("Potential Bug leading to crash");
						}
//						finally{
//							CTag.add_new(t, 0);
//						}
						
						int _id = db.Lib.get_id("tag", a.getName());
						
						if (_id != -1){
							
							String _cmd = String.format("insert into %s (%s_id, %s_id, count) values (%s, %s, %s)", listName, tableName, "tag", aId, _id, t.getCount());
							
							db.SQL.exe_insert(_cmd);
						}
						
					}
					else {
						//HF.print(String.format("---...> [IGNORED] element; name: %s", a.getName()));

					}
					
					t = null;
				}
			}
			
			HF.get_cost_p(" .. .. ALL_Operations_ALBUM_TOP_TAGS", begin);
			
			a = null;
			tags = null;
			
		} catch (Exception e) {
			Api_Catcher.album_error++;
		}
	}
	

}
