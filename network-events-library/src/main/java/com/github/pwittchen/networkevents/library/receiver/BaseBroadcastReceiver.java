/*
 * Copyright (C) 2015 Piotr Wittchen
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
package com.github.pwittchen.networkevents.library.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.NetworkState;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.squareup.otto.Bus;

public abstract class BaseBroadcastReceiver extends BroadcastReceiver {
    private final Bus bus;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public BaseBroadcastReceiver(Bus bus) {
        this.bus = bus;
    }

    @Override
    public abstract void onReceive(Context context, Intent intent);

    protected boolean statusNotChanged(ConnectivityStatus connectivityStatus) {
        return NetworkState.status == connectivityStatus;
    }

    protected void postConnectivityChanged(ConnectivityStatus connectivityStatus) {
        NetworkState.status = connectivityStatus;
        postFromAnyThread(new ConnectivityChanged(connectivityStatus));
    }

    protected void postConnectivityChanged(ConnectivityStatus connectivityStatus, Runnable onNext) {
        NetworkState.status = connectivityStatus;
        postFromAnyThread(new ConnectivityChanged(connectivityStatus));
        onNext.run();
    }

    protected void postFromAnyThread(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            bus.post(event);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    bus.post(event);
                }
            });
        }
    }
}
