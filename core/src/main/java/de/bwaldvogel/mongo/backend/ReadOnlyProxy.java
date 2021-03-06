package de.bwaldvogel.mongo.backend;

import java.time.Clock;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.bwaldvogel.mongo.MongoBackend;
import de.bwaldvogel.mongo.MongoDatabase;
import de.bwaldvogel.mongo.ServerVersion;
import de.bwaldvogel.mongo.bson.Document;
import de.bwaldvogel.mongo.exception.MongoServerException;
import de.bwaldvogel.mongo.exception.NoSuchCommandException;
import de.bwaldvogel.mongo.wire.message.MongoDelete;
import de.bwaldvogel.mongo.wire.message.MongoInsert;
import de.bwaldvogel.mongo.wire.message.MongoKillCursors;
import de.bwaldvogel.mongo.wire.message.MongoMessage;
import de.bwaldvogel.mongo.wire.message.MongoQuery;
import de.bwaldvogel.mongo.wire.message.MongoUpdate;
import io.netty.channel.Channel;

public class ReadOnlyProxy implements MongoBackend {

    private static final Set<String> allowedCommands = new HashSet<>();

    static {
        allowedCommands.add("ismaster");
        allowedCommands.add("listdatabases");
        allowedCommands.add("count");
        allowedCommands.add("dbstats");
        allowedCommands.add("distinct");
        allowedCommands.add("collstats");
        allowedCommands.add("serverstatus");
        allowedCommands.add("buildinfo");
        allowedCommands.add("getlasterror");
    }

    private final MongoBackend backend;

    public ReadOnlyProxy(MongoBackend backend) {
        this.backend = backend;
    }

    public static class ReadOnlyException extends MongoServerException {

        private static final long serialVersionUID = 4781141056923033645L;

        ReadOnlyException(String message) {
            super(message);
        }

    }

    @Override
    public void handleClose(Channel channel) {
        backend.handleClose(channel);
    }

    @Override
    public Document handleCommand(Channel channel, String database, String command, Document query) {
        if (allowedCommands.contains(command.toLowerCase())) {
            return backend.handleCommand(channel, database, command, query);
        }
        throw new NoSuchCommandException(command);
    }

    @Override
    public Document handleMessage(MongoMessage message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Document> getCurrentOperations(MongoQuery query) {
        return backend.getCurrentOperations(query);
    }

    @Override
    public QueryResult handleQuery(MongoQuery query) {
        return backend.handleQuery(query);
    }

    @Override
    public QueryResult handleGetMore(long cursorId, int numberToReturn) {
        return backend.handleGetMore(cursorId, numberToReturn);
    }

    @Override
    public void handleInsert(MongoInsert insert) {
        throw new ReadOnlyException("insert not allowed");
    }

    @Override
    public void handleDelete(MongoDelete delete) {
        throw new ReadOnlyException("delete not allowed");
    }

    @Override
    public void handleUpdate(MongoUpdate update) {
        throw new ReadOnlyException("update not allowed");
    }

    @Override
    public void dropDatabase(String database) {
        throw new ReadOnlyException("dropping of databases is not allowed");
    }

    @Override
    public MongoBackend version(ServerVersion version) {
        throw new ReadOnlyException("not supported");
    }

    @Override
    public MongoDatabase resolveDatabase(String database) {
        throw new ReadOnlyException("resolveDatabase not allowed");
    }

    @Override
    public Document getServerStatus() {
        return backend.getServerStatus();
    }

    @Override
    public void close() {
        backend.close();
    }

    @Override
    public Clock getClock() {
        return backend.getClock();
    }

    @Override
    public void enableOplog() {
    }

    @Override
    public void disableOplog() {
    }

    @Override
    public void handleKillCursors(MongoKillCursors mongoKillCursors) {
        backend.handleKillCursors(mongoKillCursors);
    }

}
