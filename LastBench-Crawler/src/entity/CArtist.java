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
import de.umass.lastfm.Artist;
import de.umass.lastfm.Event;
import de.umass.lastfm.Tag;
import de.umass.lastfm.Track;
import de.umass.lastfm.User;

public class CArtist extends CCommon{

	 final static String tableName = "artist";
     final static String cParams   = "url varchar(100), listeners int(15), playcount int(15)";
     
     public static int id;
     
     /*** CREATING TABLE AND ITS RELATIONSHIPS ***/
 	
 	public CArtist(){
 		
 		int_table(tableName, cParams);
 		int_relations_tables();
 	
 	}
 	
 	public void int_relations_tables() {

 		db.Lib.cRelTable("artist_events", tableName, "event");
 		db.Lib.cRelTable("artist_topTags", tableName, "tag");
 			db.SQL.exe_insert("ALTER TABLE artist_topTags ADD count int (15)");

		db.Lib.cRelTable("artist_topAlbums", tableName, "album");
 			
		db.Lib.cRelTable("artist_topFans", tableName, "fan");
	
		db.Lib.cRelTable("artist_similar", tableName, "similar");
			db.SQL.exe_insert("ALTER TABLE artist_similar ADD matchs int (15)");

 		db.Lib.cRelTable("artist_topTracks", tableName, "track");		
	}
 	
 	public static void add_new(Artist a, int lvl) {
		
		if ( a != null ){
			control_object(a, lvl);
		}
		
		a = null;
	}

	private static void full_info(Artist a) {
			
		String _tmp = "";
		
		_tmp = "url = '%s', listeners = '%s', playcount = '%s'";
		
		_tmp = String.format(_tmp, db.Lib.clean(a.getUrl()), Integer.toString(a.getListeners()), Integer.toString(a.getPlaycount()));

		db.Lib.update(tableName, _tmp, a.getName());
		
		a = null;
		
	}
	
	private static void control_object(Artist a, int lvl){
		
		String aName = db.Lib.clean(a.getName());
		
		boolean db_exists = db.Lib.check_exsits(tableName, aName);
		
		if (!db_exists){
			
			id = db.Lib.simpleInsert(tableName, aName);
			
			if (id != -1) {
				full_info(a);
				
				if (lvl > 0){
					db.Lib.update_to_seen(tableName, aName);
					fill_relations(id, a);
					db.Lib.update_to_completed(tableName, aName);
				}
				
			}
		}
		
		a = null;
				
	}
 	
	/****************** FILL RELATED RELATIONSHIP ******************/
	
	private static void fill_relations(int aId, Artist a) {
			
		String aName = a.getName();
		
		fill_events(aId, a, "artist_events");
		fill_top_tags(aId, a, "artist_topTags");
		fill_top_albums(aId, aName, "artist_topAlbums");
		fill_top_fans(aId, a, "artist_topFans");
		fill_top_tracks(aId, aName, "artist_topTracks");
		
		fill_similar(aId, a, "artist_similar");
		
		a = null;
		
	}

	/****************** ARTIST_SIMILAR ******************/
	
	private static void fill_similar(int aId, Artist a, String relName){
		
		Collection<Artist> similar_artist = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id, matchs) values (%s, %s, %s)";
		
		long begin;
		
		if (a != null){
			
			begin = System.currentTimeMillis();
			
			similar_artist = Artist.getSimilar(a.getName(), Config.artist_similar,  getAPI());
			
			HF.get_cost_p(" .. .. API_Call_Artist_Similar", begin);
			
			begin = System.currentTimeMillis();
			
			if ( similar_artist.size() > 0 ){
				
				for(Artist aF : similar_artist){
					
					if (aF != null){
						
						try{}
						
						finally{
							CArtist.add_new(aF, 0);
						}
						
						int _id = db.Lib.get_id("artist", aF.getName());
					
						if (_id != -1){
							
							_cmd = String.format(sql_row, relName, tableName, "similar", aId, _id, Float.toString(aF.getSimilarityMatch())); //#TODO: get info for match
							
							db.SQL.exe_insert(_cmd);
						
						}
					
					}
					aF = null;
				}
			}
			
			HF.get_cost_p(" .. .. ALL_Operations_Artist_Similar", begin);
			
		}
		
		a = null;
		similar_artist = null;
		
	}

	/****************** TOP_FANS ******************/
	
	private static void fill_top_fans(int aId, Artist a, String relName){
		
		Collection<User> top_fans = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id) values (%s, %s)";
		
		long begin;
		
		if (a != null){

			begin = System.currentTimeMillis();

			top_fans = Artist.getTopFans(a.getName(), Config.artist_topFans, getAPI()); 

			HF.get_cost_p(" .. .. API_Artist_Top_Fans", begin);
			
			begin = System.currentTimeMillis();
			
			if ( top_fans.size() > 0 ){
				
				for(User u : top_fans){
					
					if (u != null){
						
						try{}
						finally{
							CUser.add_new(u, 0); // non-recersive dsicovery for user fan
						}
						int _id = db.Lib.get_id("user", u.getName());
					
						if (_id != -1){
							
							_cmd = String.format(sql_row, relName, tableName, "fan", aId, _id);
							
							db.SQL.exe_insert(_cmd);
							
							//HF.print(_cmd);
						}
					
					}
					u = null;
				}
			}
			
			HF.get_cost_p(" .. .. ALL_Operations_Artist_Top_Fans", begin);
		}
		a = null;
		top_fans = null;
	}
	
	/****************** ARTIST_EVENTS ******************/
	
	private static void fill_events(int aId, Artist a, String relName){
			
		Collection<Event> events = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id) values (%s, %s)";

		long begin;
		
		if (a != null){

			try {

				begin = System.currentTimeMillis();
				
				events = Artist.getEvents(a.getName(), Config.artist_events, getAPI());

				HF.get_cost_p(" .. .. API_Artist_Events", begin);
				
				begin = System.currentTimeMillis();
				
				if ( events.size() > 0 ){
					
					for(Event e: events){
						
						if (e != null){
							
							try{}
							finally{
								CEvent.add_new(e);
							}
							String eName = e.getTitle();
							
							eName = db.Lib.unclean(eName);
							
							int _id = db.Lib.get_id("event", eName);
						
							if (_id != -1){
								
								_cmd = String.format(sql_row, relName, tableName, "event", aId, _id);
								
								db.SQL.exe_insert(_cmd);
								
								//HF.print(_cmd);
							}
						
						}
						e = null;
					}
				}
				
				HF.get_cost_p(" .. .. ALL_Operations_Artist_Events", begin);
				
			} catch (Exception e) {
				Api_Catcher.artist_error++;
			}
		}
		a = null;
		events = null;
	}
	
	/****************** TOP_TAGS ******************/
	
	private static void fill_top_tags(int aId, Artist a, String relName){
		
		Collection<Tag> tags = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id, count) values (%s, %s, %s)";
		
		long begin;
		
		if (a != null){

			begin = System.currentTimeMillis();
			
			tags = Artist.getTopTags(a.getName(), Config.artist_topTags, getAPI()); 

			HF.get_cost_p(" .. .. API_Artist_Top_Tags", begin);
			
			begin = System.currentTimeMillis();
			
			if ( tags.size() > 0 ){
				
				for(Tag t: tags){
					
					if (t != null){
						
						try {
							
							try{}
							finally{
								CTag.add_new(t, 0);
							}
							
							int _id = db.Lib.get_id("tag", t.getName());

							if (_id != -1){
								
								_cmd = String.format(sql_row, relName, tableName, "tag", aId, _id, t.getCount());
								
								db.SQL.exe_insert(_cmd);
								
								//HF.print(_cmd);
							}
						} catch (Exception e) {
							Api_Catcher.artist_error++;
						}
					
					}
					
					t = null;
				}
			}
			
			HF.get_cost_p(" .. .. ALL_Operations_Artist_Events", begin);
		}
		
		a = null;
		tags = null;
		
	}
	
	/****************** TOP_TRACKS ******************/
	
	private static void fill_top_tracks(int aId, String aName, String relName){
		
		Collection<Track> tracks = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id) values (%s, %s)";
			
		//HF.print("TEST, entered fill_top_taracks");

		long begin;
		
		if (aName != null){

			try {
				
				begin = System.currentTimeMillis();
				
				tracks = Artist.getTopTracks(aName, Config.artist_topTracks, getAPI()); 
				
				HF.get_cost_p(" .. .. API_Artist_Top_Tracks", begin);
				
				if ( tracks.size() > 0 ){

					for(Track t: tracks){
						
						if (t != null){
					
							try {
								
								try{}
								finally{
									CTrack.add_new(t, 0);
								}
								
								int _id = db.Lib.get_id("track", db.Lib.clean(t.getName()));

								if (_id != -1){
									
									_cmd = String.format(sql_row, relName, tableName, "track", aId, _id);
									
									db.SQL.exe_insert(_cmd);
									
									//HF.print(_cmd);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						t = null;
					}
				}
				
				HF.get_cost_p(" .. .. ALL_Operations_Artist_Top_Tracks", begin);	
				
				tracks = null;
				
			} catch (Exception e) {
				Api_Catcher.artist_error++;
			}
		}
	}
	
	/****************** TOP_ALBUMS ******************/

	private static void fill_top_albums(int aId, String aName, String relName){	
		
		Collection<Album> albums = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id) values (%s, %s)";
		
		long begin;
		
		if (aName != null){

			try {

				begin = System.currentTimeMillis();
				
				albums = Artist.getTopAlbums(aName, Config.artist_topAlbums, getAPI());

				HF.get_cost_p(" .. .. API_Artist_Top_Albums", begin);
				
				begin = System.currentTimeMillis();
				
				if (albums.size() > 0 ){
					
					for(Album aF: albums){
						
						if (aF != null){
							
							try{}
							finally{
								CAlbum.add_new(aF, 0);
							}
							
							int _id = db.Lib.get_id("album", aF.getName());
						
							if (_id != -1){
								
								_cmd = String.format(sql_row, relName, tableName, "album", aId, _id);
								
								db.SQL.exe_insert(_cmd);
								
								//HF.print(_cmd);
							}
						
						}
						
						aF = null;
					}
				}
				
				HF.get_cost_p(" .. .. ALL_Operations_Artist_Top_Albums", begin);
				
				albums = null;
				
			} catch (Exception e) {
				Api_Catcher.artist_error++;
			}
		}
	}
}
