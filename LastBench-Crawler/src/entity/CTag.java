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
import de.umass.lastfm.Tag;
import de.umass.lastfm.Track;

public class CTag extends CCommon{

	final static String tableName = "tag";
    final static String cParams = "url varchar(100), reachedUsers int(11), taggings int(11)";
    
	public static int id;
	
	
	/*** CREATING TABLE AND ITS RELATIONSHIPS ***/
	
	public CTag(){
		
		int_table(tableName, cParams);
		int_relations_tables();
	}
	
	private void int_relations_tables() {
		
		db.Lib.cRelTable("tag_topArtist", tableName, "artist");
		db.Lib.cRelTable("tag_TopTracks", tableName, "track");

		db.Lib.cRelTable("tag_TopAlbums", tableName, "album");
		
		db.Lib.cRelTable("tag_similar", tableName, "similar");

	}
	
	public static void add_new(Tag t, int lvl) {
	
		if ( t != null ){
			control_object(t, lvl);
		}
		
		t = null;
	}
	
	private static void full_info(Tag t) {
		
		String _tmp = "";

		_tmp = "url = '%s', reachedUsers = '%s', taggings = '%s'";
	
		_tmp = String.format(_tmp, t.getUrl(), Integer.toString(t.getReach()), Integer.toString(t.getTaggings()));

		db.Lib.update(tableName, _tmp, t.getName());
		
		t = null;
		
	}
	
	private static void control_object(Tag t, int lvl){
	
		String tName = db.Lib.clean(t.getName());
		
		boolean db_exists = db.Lib.check_exsits(tableName, tName);
		
		if (!db_exists){
			
			id = db.Lib.simpleInsert(tableName, tName);
			
			if (id != -1){
				
				full_info(t);
				
				if (lvl > 0){
					db.Lib.update_to_seen(tableName, tName);
					fill_relations(id, t, lvl);
					db.Lib.update_to_completed(tableName, tName);
				}
			}
		}
		
		t = null;
		
	}

	private static void fill_relations(int aId, Tag t, int lvl) {
		
		String tName = t.getName();

		fill_tag_similar(aId, t, "tag_similar");
		fill_tag_tag_TopAlbums(aId, tName, "tag_TopAlbums");
		fill_tag_tag_TopArtist(aId, tName, "tag_topArtist");
		fill_tag_tag_TopTracks(aId, tName, "tag_TopTracks");
		
	}
	
	/****************** TAG_SIMILAR ******************/
	
	private static void fill_tag_similar(int aId, Tag t, String relName) {

		Collection<Tag> similar_tag = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id) values (%s, %s)";
		
		if (t != null){
			
			try {
				long begin; 

				begin = System.currentTimeMillis();
				
				similar_tag = Tag.getSimilar(t.getName(), Config.tag_similar, getAPI());  
				
				HF.get_cost_p(" .. .. API_Call_Tag_Similar", begin);
				
				begin = System.currentTimeMillis();
				
				if ( similar_tag.size() > 0 ){
					
					for(Tag tF : similar_tag){
						
						if (tF != null){
							
							try{}
							finally{
								CTag.add_new(tF, 0);
							}
							
							int _id = db.Lib.get_id("tag", tF.getName());
						
							if (_id != -1){
								
								_cmd = String.format(sql_row, relName, tableName, "similar", aId, _id); 
								
								db.SQL.exe_insert(_cmd);
							
							}
						
						}
						
						tF = null;
					}
				}
				
				HF.get_cost_p(" .. .. ALL_Operations_Tag_Similar", begin);
				
			} catch (Exception e) {
				Api_Catcher.tag_error++;
			}
		}
		
		similar_tag = null;

	}
	
	/****************** TOP_ARTIST ******************/
	
	private static void fill_tag_tag_TopArtist(int aId, String tName, String relName) {
		
		Collection<Artist> artists = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id) values (%s, %s)";
		
		if (tName != null){

			try {
				
				long begin; 

				begin = System.currentTimeMillis();

				HF.get_cost_p(" .. .. API_Call_Tag_Top_Artist", begin);

				artists = Tag.getTopArtists(tName, Config.tag_topArtist, getAPI()); 

				begin = System.currentTimeMillis();
					
				if (artists.size() > 0 ){
					
					for(Artist aF: artists){
						
						if (aF != null){
							
							try{}
							finally{
								CArtist.add_new(aF, 0);
							}
							
							int _id = db.Lib.get_id("artist", aF.getName());
						
							if (_id != -1){
								
								_cmd = String.format(sql_row, relName, tableName, "artist", aId, _id);
								
								db.SQL.exe_insert(_cmd);
								
								//HF.print(_cmd);
							}
						
						}
						
						aF = null;
					}
				}
				
				HF.get_cost_p(" .. .. ALL_Operations_Tag_Top_Artist", begin);
				
			} catch (Exception e) {
				Api_Catcher.tag_error++;
			}
		}
		
		artists = null;
		
	}

	/****************** TOP_TRACK ******************/
	
	private static void fill_tag_tag_TopTracks(int aId, String tName, String relName) {
		
		Collection<Track> tracks = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id) values (%s, %s)";
		
		if (tName != null){

			try {
				long begin; 

				begin = System.currentTimeMillis();

				HF.get_cost_p(" .. .. API_Call_TAG_TOP_TRACK", begin);

				tracks = Tag.getTopTracks(tName, Config.tag_TopTracks, getAPI()); 
				
				if (tracks.size() > 0 ){
					
					for(Track tF: tracks){
						
						if (tF != null){
							
							try{}
							finally{
								CTrack.add_new(tF, 0);								
							}

							
							int _id = db.Lib.get_id("track", tF.getName());
						
							if (_id != -1){
								
								_cmd = String.format(sql_row, relName, tableName, "track", aId, _id);
								
								db.SQL.exe_insert(_cmd);
								
								//HF.print(_cmd);
							}
						
						}
						tF = null;
					}
				}
				
				begin = System.currentTimeMillis();

				HF.get_cost_p(" .. .. ALL_Operations_TAG_TOP_TRACK", begin);
				
			} catch (Exception e) {
				Api_Catcher.tag_error++;
			}
		}
		
		tracks = null;
		
	}

	/****************** TOP_ALBUM ******************/
	
	private static void fill_tag_tag_TopAlbums(int aId, String tName, String relName) {
		
		Collection<Album> albums = null;
		
		String sql_row = null, _cmd = null;
		
		sql_row = "insert into %s (%s_id, %s_id, similarity_score) values (%s, %s, %s)";
		
		if (tName != null){

			try {
				long begin; 

				begin = System.currentTimeMillis();

				albums = Tag.getTopAlbums(tName, Config.tag_TopAlbums, getAPI()); 
				
				HF.get_cost_p(" .. .. API_Call_Tag_Top_Album", begin);
				
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
								
								_cmd = String.format(sql_row, relName, "tag", "album", aId, _id, aF.getSimilarityMatch());
								
								db.SQL.exe_insert(_cmd);
								
								//HF.print(_cmd);
							}
						
						}
						
						aF = null;
					}
				}
				
				HF.get_cost_p(" .. .. ALL_Operations_Tag_Top_Album", begin);
				
			} catch (Exception e) {
				Api_Catcher.tag_error++;
			}
		}
		
		albums = null;
		
	}



}
