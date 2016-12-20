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
import de.umass.lastfm.Tag;
import de.umass.lastfm.Track;
import de.umass.lastfm.User;

public class CTrack extends CCommon{

	final static String tableName = "track";
    final static String cParams   = "url varchar(100), duration int(50), listeners int(10), playcount int(10), album varchar(255), artistMbID varchar(100), albumMbID varchar(100), visited int(1) DEFAULT 0";
    
    public static int id;
    
    /*** CREATING TABLE AND ITS RELATIONSHIPS ***/
	
	public CTrack(){
		
		int_table(tableName, cParams);
		int_relations_tables();
	}
	
	private void int_relations_tables() {
		
		db.Lib.cRelTable("track_topFans", tableName, "fan");
		db.Lib.cRelTable("track_similar", tableName, "similar");
			db.SQL.exe_insert("ALTER TABLE track_similar ADD matchs int (15)");
		
		db.Lib.cRelTable("track_topTags", tableName, "tag");
			db.SQL.exe_insert("ALTER TABLE track_topTags ADD count int (15)");
		
	}
	
	public static void add_new(Track t, int lvl) {
		
		if ( t != null ){
			try{}finally{
				control_object(t, lvl);	
				t = null;
			}
		}
	}
	
	private static void full_info(Track t) {
		
		try {
			if (t != null){
				
				String _tmp = "";
				
				_tmp = "url = '%s', duration = '%s', listeners  = '%s', playcount = '%s', album  = '%s', artistMbID  = '%s', albumMbID  = '%s', visited = '%s'";
				
				_tmp = String.format(_tmp, t.getUrl(), t.getDuration(), t.getListeners(), t.getPlaycount(), "x", t.getArtistMbid(), "x", Integer.toString(1));
				
				db.Lib.update(tableName, _tmp, t.getName());
				
				t = null;
			}

		} catch (Exception e) {
			System.out.println(String.format("Error occured at full_info {CTtack} :: %s", e));
		}
		
	}
	
	private static void control_object(Track t, int lvl){
		
		String tName = db.Lib.clean(t.getName());
		
		boolean db_exists = db.Lib.check_exsits(tableName, tName);
		
		if (!db_exists){
			
			id = db.Lib.simpleInsert(tableName, tName);

			if (id  != -1 ){

				full_info(t); 
				
				if (lvl > 0){

					db.Lib.update_to_seen(tableName, tName);
					
					try{}finally{
						fill_relations(id, t, lvl);
					}
					
					db.Lib.update_to_completed(tableName, tName);
					t = null;
				}
				
			}
		}
	}
	
	private static void fill_relations(int aId, Track t, int lvl) {

		fill_track_similar(aId, t, "track_similar");
		fill_track_topTags(aId, t, "track_topTags");
		fill_track_topFans(aId, t, "track_topFans");
		
	}

	/****************** TRACK_SIMILAR ******************/

	private static void fill_track_similar(int aId, Track t, String relName) {
		
		Collection<Track> similar_track = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id, matchs) values (%s, %s, %s)";
		
		if (t != null){
			
			try {
				
				long begin; 

				begin = System.currentTimeMillis();

				HF.get_cost_p(" .. .. API_Call_Track_Similar", begin);

				similar_track = Track.getSimilar(t.getArtist(), t.getName(), Config.tag_similar, getAPI());  //#TODO adjust limits
				
				begin = System.currentTimeMillis();

				if ( similar_track.size() > 0 ){
					
					for(Track tF : similar_track){
						
						if (tF != null){
							
							try{}finally{
								CTrack.add_new(tF, 0);								
							}

							
							int _id = db.Lib.get_id("track", tF.getName());
						
							if (_id != -1){
								
								_cmd = String.format(sql_row, relName, tableName, "similar", aId, _id, tF.getSimilarityMatch()); 
								
								db.SQL.exe_insert(_cmd);
							
							}
						
						}
						
						tF = null;
					}
					
				}
				
				HF.get_cost_p(" .. .. ALL_Operations_Track_Similar", begin);
				t = null;
			} catch (Exception e) {
				Api_Catcher.track_error++;
			}
		}
		similar_track = null;
		
	}

	/****************** TOP_TAGS ******************/
	
	private static void fill_track_topTags(int aId, Track t, String relName) {
		
		Collection<Tag> top_tags = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id, count) values (%s, %s, %s)";
		
		if (t != null){
			
			try {
				long begin; 

				begin = System.currentTimeMillis();

				top_tags = Track.getTopTags(t.getArtist(), t.getMbid(), Config.track_topTags, getAPI()); 

				HF.get_cost_p(" .. .. API_Call_TRACK_TOP_TAGS", begin);
				
				begin = System.currentTimeMillis();

				if ( top_tags.size() > 0 ){
					
					for(Tag tF : top_tags){
						
						if (tF != null){
							
							try{}
							finally{
								CTag.add_new(tF, 0);	
							}
							
							int _id = db.Lib.get_id("tag", tF.getName());
						
							if (_id != -1){
								
								_cmd = String.format(sql_row, relName, tableName, "tag", aId, _id, Integer.toString(tF.getCount()));							
								db.SQL.exe_insert(_cmd);
							
							}
						
						}
						tF = null;
					}
					
				}
				
				HF.get_cost_p(" .. .. ALL_Operations_TRACK_TOP_TAGS", begin);
				t = null;
				
			} catch (Exception e) {
				Api_Catcher.track_error++;
			}
		}
		
		top_tags = null;
	 }
	
	/****************** TOP_FANS ******************/
	
	private static void fill_track_topFans(int aId, Track t, String relName) {
		
		Collection<User> top_fans = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id) values (%s, %s)";
		
		if (t != null){
			
			try {

				long begin; 

				begin = System.currentTimeMillis();

				top_fans = Track.getTopFans(t.getArtist(), t.getMbid(), Config.track_topFans, getAPI());
				
				HF.get_cost_p(" .. .. API_Call_Track_Top_Fans", begin);
				
				begin = System.currentTimeMillis();

				if ( top_fans.size() > 0 ){
					
					for(User uF : top_fans){
						
						if (uF != null){
							
							try{}finally{
								CUser.add_new(uF, 0);	
							}
							
							
							int _id = db.Lib.get_id("user", uF.getName());
						
							if (_id != -1){
								
								_cmd = String.format(sql_row, relName, tableName, "fan", aId, _id);
								
								db.SQL.exe_insert(_cmd);
							
							}
						
						}
						uF = null;
					}
					
				}
				
				HF.get_cost_p(" .. .. ALL_Operations_Track_Top_Fans", begin);
				t = null;
			} catch (Exception e) {
				Api_Catcher.track_error++;
			}
			
		}
		top_fans = null;
	}
	
}
