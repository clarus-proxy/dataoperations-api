package eu.clarussecure.dataoperations.geometry;

import java.sql.SQLException;

import org.postgis.Geometry;
import org.postgis.PGbox2d;
import org.postgis.PGbox3d;
import org.postgis.PGboxbase;
import org.postgis.PGgeometry;
import org.postgis.binary.BinaryWriter;
import org.postgis.binary.ByteGetter;
import org.postgis.binary.ValueGetter;

public class GeometryBuilder {
    private boolean wktFormat = true;
    private boolean byteOrderBigEndian = true;

    public Object decode(String value) {
        Object instance = decodeGeometry(value);

        if (instance == null) {
            instance = decodePGboxbase(value);
        }

        return instance;
    }

    public Geometry decodeGeometry(String value) {
        Geometry geometry = null;

        try {
            geometry = PGgeometry.geomFromString(value);
            if (value.startsWith(PGgeometry.SRIDPREFIX)) {
                // break up geometry into srid and wkt or wkb
                String[] parts = PGgeometry.splitSRID(value);
                value = parts[1].trim();
            }
            if (value.startsWith("00") || value.startsWith("01")) {
                wktFormat = false;
                ByteGetter.StringByteGetter bytes = new ByteGetter.StringByteGetter(value);
                byteOrderBigEndian = bytes.get(0) == ValueGetter.XDR.NUMBER;
            }
        } catch (SQLException e) {
            // nothing to do
        }
        return geometry;
    }

    public PGboxbase decodePGboxbase(String value) {
        PGboxbase boxbase = null;

        try {
            String prefix = value;
            if (prefix.startsWith("SRID=")) {
                String[] temp = PGgeometry.splitSRID(prefix);
                prefix = temp[1].trim();
            }
            int index = prefix.indexOf('(');
            prefix = prefix.substring(0, index).trim();
            String prefix2d = new PGbox2d().getPrefix().toLowerCase();
            String prefix3d = new PGbox3d().getPrefix().toLowerCase();
            if (prefix3d.equalsIgnoreCase(prefix)) {
                boxbase = new PGbox3d(value);
            } else if (prefix2d.equalsIgnoreCase(prefix)) {
                boxbase = new PGbox2d(value);
            }
        } catch (SQLException e) {
            // nothing to do
        }

        return boxbase;
    }

    public String encode(Object instance) {
        String value = instance instanceof Geometry ? encodeGeometry((Geometry) instance)
                : instance instanceof PGboxbase ? encodePGboxbase((PGboxbase) instance) : null;
        return value;
    }

    public String encodeGeometry(Geometry geometry) {
        String value = null;

        if (wktFormat) {
            value = geometry.toString();
        } else {
            BinaryWriter bw = new BinaryWriter();
            value = bw.writeHexed(geometry, byteOrderBigEndian ? ValueGetter.XDR.NUMBER : ValueGetter.NDR.NUMBER);
        }
        return value;
    }

    public String encodePGboxbase(PGboxbase boxbase) {
        String value;
        int srid = boxbase.getLLB().getSrid();
        if (srid != 0) {
            value = String.format("SRID=%d;%s", srid, boxbase.toString());
        } else {
            value = boxbase.toString();
        }
        return value;
    }

}
