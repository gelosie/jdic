/**
 * 
 */
package org.jdesktop.jdic.mpcontrol.itunes;

import org.jdesktop.jdic.mpcontrol.IExtendedSongInfo;

/**
 * @author Zsombor
 *
 */
class ITunesSongInfo implements IExtendedSongInfo {

	private String album;
	private String artist;
	private String title;
	private String path;
	private int trackNumber;

	
	
	
	public ITunesSongInfo(String album, String artist, String title, int number,String path ) {
		this.album = album;
		this.artist = artist;
		this.path = path;
		this.title = title;
		trackNumber = number;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IExtendedSongInfo#getAlbum()
	 */
	public String getAlbum() {
		return album;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IExtendedSongInfo#getArtist()
	 */
	public String getArtist() {
		return artist;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IExtendedSongInfo#getTitle()
	 */
	public String getTitle() {
		return title;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.ISongInfo#getSongTitle()
	 */
	public String getSongTitle() {
		return artist +  " - "+album +" - " + title;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.ISongInfo#getPath()
	 */
	public String getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.ISongInfo#getTrackNumber()
	 */
	public int getTrackNumber() {
		return trackNumber;
	}

}
