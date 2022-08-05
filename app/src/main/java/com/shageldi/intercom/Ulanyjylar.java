package com.shageldi.intercom;

import java.net.InetAddress;

public class Ulanyjylar {
    String name;
    InetAddress address;

    public Ulanyjylar(String name, InetAddress address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }
}
