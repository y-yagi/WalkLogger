package io.github.y_yagi.walklogger.model;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by yaginuma on 17/06/23.
 */

public class Migration implements RealmMigration {
    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
        // Access the Realm schema in order to create, modify or delete classes and their fields.
        RealmSchema schema = realm.getSchema();

        // Migrate from version 0 to version 1
        // Add "Waypoint" schema
        // Add "waypoints" to "Walk" schema
        if (oldVersion == 0) {
            schema.create("Waypoint")
                    .addField("uid", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("memo", String.class)
                    .addField("latitude", double.class)
                    .addField("longitude", double.class);

            RealmObjectSchema walkSchema = schema.get("Walk");
            walkSchema.addRealmListField("waypoints", schema.get("Waypoint"));
            oldVersion++;
        }

        if (oldVersion == 1) {
            RealmObjectSchema waypointSchema = schema.get("Waypoint");
            waypointSchema.removeField("uid");
            waypointSchema.addField("uuid", String.class, FieldAttribute.PRIMARY_KEY);
            oldVersion++;
        }
    }

    public int hashCode() {
        return Migration.class.hashCode();
    }

    public boolean equals(Object object) {
        if(object == null) {
            return false;
        }
        return object instanceof Migration;
    }
}
