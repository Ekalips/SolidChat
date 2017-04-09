package com.andre.solid.solidchat.data;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by lantain on 08.04.17.
 */

public class PartnerUserData extends RealmObject {
    @PrimaryKey
    private
    String address;
    private String deviceName;
    private String name;
    private boolean isInNetwork;
    private int connectionStatus;
    private String image;
    private RealmList<QuickQuestion> quickQuestions;

    public PartnerUserData() {
    }

    public PartnerUserData(String address, String deviceName, int connectionStatus) {
        this.address = address;
        this.deviceName = deviceName;
        this.connectionStatus = connectionStatus;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDisplayName(){
        if (name!=null && !name.isEmpty()){
            return name;
        }
        else
            return getDeviceName();
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInNetwork() {
        return isInNetwork;
    }

    public void setInNetwork(boolean inNetwork) {
        isInNetwork = inNetwork;
    }

    public String getConnectionStatusString(){
        return ConnectionStatus.fromInt(connectionStatus).toString();
    }

    public int getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(int connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PartnerUserData that = (PartnerUserData) o;

        return address != null ? address.equals(that.address) : that.address == null;

    }

    @Override
    public int hashCode() {
        return address != null ? address.hashCode() : 0;
    }

    public RealmList<QuickQuestion> getQuickQuestions() {
        return quickQuestions;
    }

    public void setQuickQuestions(RealmList<QuickQuestion> quickQuestions) {
        this.quickQuestions.clear();
        this.quickQuestions.addAll(quickQuestions);
    }

    public enum ConnectionStatus{
        connected(0),invited(1),failed(2),available(3),unavailable(4), undefined(-1);
//        public static final int CONNECTED   = 0;
//        public static final int INVITED     = 1;
//        public static final int FAILED      = 2;
//        public static final int AVAILABLE   = 3;
//        public static final int UNAVAILABLE = 4;

        int status;
        ConnectionStatus(int status) {
            this.status = status;
        }

        public static ConnectionStatus fromInt(int status){
            for (ConnectionStatus connectionStatus :
                    ConnectionStatus.values()) {
                if (connectionStatus.status == status){
                    return connectionStatus;
                }
            }
            return undefined;
        }



        public int getStatus() {
            return status;
        }

        @Override
        public String toString() {
            return super.toString().toUpperCase();
        }

        public int toInt() {
            return status;
        }
    }
}
