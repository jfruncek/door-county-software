package com.door.mytunes;

import com.u2d.model.AbstractComplexEObject;
import com.u2d.model.Title;
import com.u2d.type.AbstractChoiceEO;
import com.u2d.type.atom.StringEO;
import javax.persistence.Entity;

/**
 *
 * @author jfruncek
 */
@Entity
public class Genre extends AbstractChoiceEO {

    private final StringEO _code = new StringEO();
    private final StringEO _caption = new StringEO();
    public static String[] identities = {"code"};

    public Genre() {
    }

    public Genre(String code, String caption) {
        _code.setValue(code);
        _caption.setValue(caption);
    }

    public StringEO getCaption() {
        return _caption;
    }

    public StringEO getCode() {
        return _code;
    }
}
