//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.eclipse.paho.android.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import java.util.Iterator;
import java.util.UUID;
import org.eclipse.paho.android.service.MessageStore.StoredMessage;
import org.eclipse.paho.client.mqttv3.MqttMessage;

class DatabaseMessageStore implements MessageStore {
    private static final String TAG = "DatabaseMessageStore";
    private static final String MTIMESTAMP = "mtimestamp";
    private static final String ARRIVED_MESSAGE_TABLE_NAME = "MqttArrivedMessageTable";
    private SQLiteDatabase db = null;
    private DatabaseMessageStore.MQTTDatabaseHelper mqttDb = null;
    private MqttTraceHandler traceHandler = null;

    public DatabaseMessageStore(MqttService service, Context context) {
        this.traceHandler = service;
        this.mqttDb = new DatabaseMessageStore.MQTTDatabaseHelper(this.traceHandler, context);
        this.traceHandler.traceDebug("DatabaseMessageStore", "DatabaseMessageStore<init> complete");
    }

    public String storeArrived(String clientHandle, String topic, MqttMessage message) {
        this.db = this.mqttDb.getWritableDatabase();
        this.traceHandler.traceDebug("DatabaseMessageStore", "storeArrived{" + clientHandle + "}, {" + message.toString() + "}");
        byte[] payload = message.getPayload();
        int qos = message.getQos();
        boolean retained = message.isRetained();
        boolean duplicate = message.isDuplicate();
        ContentValues values = new ContentValues();
        String id = UUID.randomUUID().toString();
        values.put("messageId", id);
        values.put("clientHandle", clientHandle);
        values.put("destinationName", topic);
        values.put("payload", payload);
        values.put("qos", qos);
        values.put("retained", retained);
        values.put("duplicate", duplicate);
        values.put("mtimestamp", System.currentTimeMillis());

        try {
            this.db.insertOrThrow("MqttArrivedMessageTable", (String)null, values);
        } catch (SQLException var11) {
            this.traceHandler.traceException("DatabaseMessageStore", "onUpgrade", var11);
            throw var11;
        }

        int count = this.getArrivedRowCount(clientHandle);
        this.traceHandler.traceDebug("DatabaseMessageStore", "storeArrived: inserted message with id of {" + id + "} - Number of messages in database for this clientHandle = " + count);
        return id;
    }

    private int getArrivedRowCount(String clientHandle) {
        int count = 0;
        String[] projection = new String[]{"messageId"};
        String selection = "clientHandle=?";
        String[] selectionArgs = new String[]{clientHandle};
        Cursor c = this.db.query("MqttArrivedMessageTable", projection, selection, selectionArgs, (String)null, (String)null, (String)null);
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }

        c.close();
        return count;
    }

    public boolean discardArrived(String clientHandle, String id) {
        this.db = this.mqttDb.getWritableDatabase();
        this.traceHandler.traceDebug("DatabaseMessageStore", "discardArrived{" + clientHandle + "}, {" + id + "}");
        String[] selectionArgs = new String[]{id, clientHandle};

        int rows;
        try {
            rows = this.db.delete("MqttArrivedMessageTable", "messageId=? AND clientHandle=?", selectionArgs);
        } catch (SQLException var6) {
            this.traceHandler.traceException("DatabaseMessageStore", "discardArrived", var6);
            throw var6;
        }

        if (rows != 1) {
            this.traceHandler.traceError("DatabaseMessageStore", "discardArrived - Error deleting message {" + id + "} from database: Rows affected = " + rows);
            return false;
        } else {
            int count = this.getArrivedRowCount(clientHandle);
            this.traceHandler.traceDebug("DatabaseMessageStore", "discardArrived - Message deleted successfully. - messages in db for this clientHandle " + count);
            return true;
        }
    }

    public Iterator<StoredMessage> getAllArrivedMessages(final String clientHandle) {
        return new Iterator<StoredMessage>() {
            private Cursor c;
            private boolean hasNext;
            private final String[] selectionArgs = new String[]{clientHandle};

            {
                DatabaseMessageStore.this.db = DatabaseMessageStore.this.mqttDb.getWritableDatabase();
                if (clientHandle == null) {
                    this.c = DatabaseMessageStore.this.db.query("MqttArrivedMessageTable", (String[])null, (String)null, (String[])null, (String)null, (String)null, "mtimestamp ASC");
                } else {
                    this.c = DatabaseMessageStore.this.db.query("MqttArrivedMessageTable", (String[])null, "clientHandle=?", this.selectionArgs, (String)null, (String)null, "mtimestamp ASC");
                }

                this.hasNext = this.c.moveToFirst();
            }

            public boolean hasNext() {
                if (!this.hasNext) {
                    this.c.close();
                }

                return this.hasNext;
            }

            public StoredMessage next() {
                String messageId = this.c.getString(this.c.getColumnIndex("messageId"));
                String clientHandlex = this.c.getString(this.c.getColumnIndex("clientHandle"));
                String topic = this.c.getString(this.c.getColumnIndex("destinationName"));
                byte[] payload = this.c.getBlob(this.c.getColumnIndex("payload"));
                int qos = this.c.getInt(this.c.getColumnIndex("qos"));
                boolean retained = Boolean.parseBoolean(this.c.getString(this.c.getColumnIndex("retained")));
                boolean dup = Boolean.parseBoolean(this.c.getString(this.c.getColumnIndex("duplicate")));
                DatabaseMessageStore.MqttMessageHack message = DatabaseMessageStore.this.new MqttMessageHack(payload);
                message.setQos(qos);
                message.setRetained(retained);
                message.setDuplicate(dup);
                this.hasNext = this.c.moveToNext();
                return DatabaseMessageStore.this.new DbStoredData(messageId, clientHandlex, topic, message);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            protected void finalize() throws Throwable {
                this.c.close();
                super.finalize();
            }
        };
    }

    public void clearArrivedMessages(String clientHandle) {
        this.db = this.mqttDb.getWritableDatabase();
        String[] selectionArgs = new String[]{clientHandle};
        int rows;
        if (clientHandle == null) {
            this.traceHandler.traceDebug("DatabaseMessageStore", "clearArrivedMessages: clearing the table");
            rows = this.db.delete("MqttArrivedMessageTable", (String)null, (String[])null);
        } else {
            this.traceHandler.traceDebug("DatabaseMessageStore", "clearArrivedMessages: clearing the table of " + clientHandle + " messages");
            rows = this.db.delete("MqttArrivedMessageTable", "clientHandle=?", selectionArgs);
        }

        this.traceHandler.traceDebug("DatabaseMessageStore", "clearArrivedMessages: rows affected = " + rows);
    }

    public void close() {
        if (this.db != null) {
            this.db.close();
        }

    }

    private class MqttMessageHack extends MqttMessage {
        public MqttMessageHack(byte[] payload) {
            super(payload);
        }

        protected void setDuplicate(boolean dup) {
            super.setDuplicate(dup);
        }
    }

    private class DbStoredData implements StoredMessage {
        private String messageId;
        private String clientHandle;
        private String topic;
        private MqttMessage message;

        DbStoredData(String messageId, String clientHandle, String topic, MqttMessage message) {
            this.messageId = messageId;
            this.topic = topic;
            this.message = message;
        }

        public String getMessageId() {
            return this.messageId;
        }

        public String getClientHandle() {
            return this.clientHandle;
        }

        public String getTopic() {
            return this.topic;
        }

        public MqttMessage getMessage() {
            return this.message;
        }
    }

    private static class MQTTDatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG = "MQTTDatabaseHelper";
        private static final String DATABASE_NAME = "mqttAndroidService.db";
        private static final int DATABASE_VERSION = 1;
        private MqttTraceHandler traceHandler = null;

        public MQTTDatabaseHelper(MqttTraceHandler traceHandler, Context context) {
            super(context, "mqttAndroidService.db", (CursorFactory)null, 1);
            this.traceHandler = traceHandler;
        }

        public void onCreate(SQLiteDatabase database) {
            String createArrivedTableStatement = "CREATE TABLE MqttArrivedMessageTable(messageId TEXT PRIMARY KEY, clientHandle TEXT, destinationName TEXT, payload BLOB, qos INTEGER, retained TEXT, duplicate TEXT, mtimestamp INTEGER);";
            this.traceHandler.traceDebug("MQTTDatabaseHelper", "onCreate {" + createArrivedTableStatement + "}");

            try {
                database.execSQL(createArrivedTableStatement);
                this.traceHandler.traceDebug("MQTTDatabaseHelper", "created the table");
            } catch (SQLException var4) {
                this.traceHandler.traceException("MQTTDatabaseHelper", "onCreate", var4);
                throw var4;
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            this.traceHandler.traceDebug("MQTTDatabaseHelper", "onUpgrade");

            try {
                db.execSQL("DROP TABLE IF EXISTS MqttArrivedMessageTable");
            } catch (SQLException var5) {
                this.traceHandler.traceException("MQTTDatabaseHelper", "onUpgrade", var5);
                throw var5;
            }

            this.onCreate(db);
            this.traceHandler.traceDebug("MQTTDatabaseHelper", "onUpgrade complete");
        }
    }
}
