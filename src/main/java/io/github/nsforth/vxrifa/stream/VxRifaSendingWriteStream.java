/*
 * Copyright (C) 2017 Nikita Staroverov.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package io.github.nsforth.vxrifa;

import io.github.nsforth.vxrifa.message.RIFAMessage;
import io.github.nsforth.vxrifa.message.RIFAReply;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.streams.WriteStream;

/**
 *
 * @author Nikita Staroverov
 */
public class VxRifaSendingWriteStream<T> implements WriteStream<T> {

    private static final int ACK_WINDOW = 100;

    private final Vertx vertx;
    private final MessageConsumer<RIFAMessage> controlConsumer;
    private String dataAddress;
    private int maxQueueSize = ACK_WINDOW;
    private long sentCounter;
    private long ackCounter;
    private Handler<Void> drainHandler;
    private Handler<Throwable> excHandler;

    public VxRifaSendingWriteStream(Vertx vertx, final String controlAddress, String remoteAddress, RIFAMessage params) {
        this.vertx = vertx;
        controlConsumer = vertx.eventBus().consumer(controlAddress, msg -> receiveControlMessage(msg.body()));
        controlConsumer.completionHandler(result -> {
            if (result.failed()) {
                closeExceptionally(result.cause());
            } else {
                vertx.eventBus().request(remoteAddress, params, new DeliveryOptions().addHeader("ControlAddress", controlAddress), reply -> {
                    if (reply.succeeded()) {
                        RIFAReply rifaReply = (RIFAReply) reply.result().body();
                        if (rifaReply.isExceptional()) {
                            closeExceptionally(result.cause());
                        } else {
                            dataAddress = (String) rifaReply.getResult();
                            vertx.eventBus().send(dataAddress, RIFAMessage.of("SetQueueSize", maxQueueSize));
                            if (this.drainHandler != null) {
                                this.drainHandler.handle(null);
                            }
                        }
                    } else {
                        closeExceptionally(result.cause());
                    }
                });
            }
        });
    }

    @Override
    public Future<Void> write(T data) {
        checkDataAddress();
        sentCounter++;
        vertx.eventBus().send(dataAddress, RIFAMessage.of("Data", data));
        return null;
    }

    @Override
    public void write(T data, Handler<AsyncResult<Void>> handler) {
        checkDataAddress();
        sentCounter++;
        vertx.eventBus().request(dataAddress, RIFAMessage.of("Data", data), (e) -> {
            handler.handle(null);
        });
    }
    
    @Override
    public Future<Void> end() {
        checkDataAddress();
        controlConsumer.unregister();
        vertx.eventBus().send(dataAddress, RIFAMessage.of("End"));
        return null;
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
        checkDataAddress();
        controlConsumer.unregister();
        vertx.eventBus().request(dataAddress, RIFAMessage.of("End"), (e) -> {
            handler.handle(null);
        });
    }
    
    @Override
    public WriteStream<T> exceptionHandler(Handler<Throwable> handler) {
        this.excHandler = handler;
        return this;
    }
    
    @Override
    public WriteStream<T> drainHandler(Handler<Void> handler) {
        this.drainHandler = handler;
        return this;
    }

    @Override
    public WriteStream<T> setWriteQueueMaxSize(int maxSize) {        
        this.maxQueueSize = maxSize;
        if (this.dataAddress != null) {
            vertx.eventBus().send(dataAddress, RIFAMessage.of("SetQueueSize", maxSize));
        }
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        if (dataAddress == null) {
            return true;
        }
        return sentCounter - ackCounter >= maxQueueSize;
    }
    
    private void receiveControlMessage(RIFAMessage rifaMessage) {
        String messageType = rifaMessage.getSuffix();
        if ("Ack".equals(messageType)) {
            boolean wasFull = writeQueueFull();
            this.ackCounter = (long) rifaMessage.getParameter(0);
            boolean nowFull = writeQueueFull();
            if (wasFull && !nowFull) {
                if (this.drainHandler == null) {
                    return;
                }
                this.drainHandler.handle(null);
            }
        } else if ("Exception".equals(messageType)) {
            closeExceptionally((Throwable) rifaMessage.getParameter(0));
        }
    }
    
    private void closeExceptionally(Throwable ex) {
        controlConsumer.unregister();
        if (this.excHandler != null) {
            this.excHandler.handle(ex);
        }
    }
    
    private void checkDataAddress() throws IllegalStateException {
        if (dataAddress == null) {
            IllegalStateException ex = new IllegalStateException("WriteStream is not ready!");
            if (this.excHandler != null) {
                this.excHandler.handle(ex);
            } else {
                throw ex;
            }            
        }
    }

}
