package com.door.mytunes;

import com.u2d.model.AbstractComplexEObject;
import com.u2d.model.Title;
import com.u2d.type.atom.StringEO;
import com.u2d.type.atom.TimeEO;
import javax.persistence.Entity;

/**
 *
 * @author jfruncek
 */
@Entity
public class Song extends AbstractComplexEObject {

    private final StringEO _title = new StringEO();
    private final TimeEO _duration = new TimeEO();
    private Album _album;
    private Artist _artist;
    private final Genre _genre = new Genre();
    
    public static final String[] fieldOrder = {"title", "duration", "artist", "album", "genre"};


    public Song() {
    }

    public StringEO getTitle() {
        return _title;
    }

    public Title title() {
        return _title.title();
    }
}
