package com.shageldi.intercom;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    ArrayList<Ulanyjylar> users=new ArrayList<>();
    Context context;
    private static final String LOG_TAG="UserAdapter";
    public UsersAdapter(ArrayList<Ulanyjylar> users, Context context) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.users_design,parent,false);
        return new UsersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            final Ulanyjylar u = users.get(position);
            holder.name.setText(u.getName());
            holder.ip.setText(u.getAddress().getHostAddress());
            holder.name.setTextColor(Color.GRAY);
            holder.ip.setTextColor(Color.GRAY);

            holder.call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessage("NOT:" + u.getAddress().getHostAddress(), 40005);
                }
            });
        } catch (IndexOutOfBoundsException ex){
            ex.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name,ip;
        ImageButton call;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            call=itemView.findViewById(R.id.call);
            name=itemView.findViewById(R.id.name);
            ip=itemView.findViewById(R.id.ip);
        }
    }

    private void sendMessage(final String message, final int port) {
        // Creates a thread used for sending notifications
        Thread replyThread = new Thread(new Runnable() {

            @Override
            public void run() {
                InetAddress broadIp=null;
                try {

                    String myIp=getCurrentIp();
                    broadIp=getBroadcast(InetAddress.getByName(myIp));
                    if(broadIp==null){
                        broadIp=getBroadcast(getIpAddressNew());
                        myIp=getIpAddressNew().getHostAddress();
                    }
                    InetAddress address = broadIp;
                    byte[] data = message.getBytes();
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                    socket.send(packet);
                    Log.i(LOG_TAG, "Sent message( " + message + " ) to " + address.getHostAddress());
                    socket.disconnect();
                    socket.close();
                }
                catch(UnknownHostException e) {

                    Log.e(LOG_TAG, "Failure. UnknownHostException in sendMessage: " + broadIp.getHostAddress());
                }
                catch(SocketException e) {

                    Log.e(LOG_TAG, "Failure. SocketException in sendMessage: " + e);
                }
                catch(IOException e) {

                    Log.e(LOG_TAG, "Failure. IOException in sendMessage: " + e);
                } catch (NullPointerException ex){
                    ex.printStackTrace();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        replyThread.start();
    }

    public InetAddress getIpAddressNew() {
        InetAddress inetAddress = null;
        InetAddress myAddr = null;

        try {
            for (Enumeration<NetworkInterface> networkInterface = NetworkInterface
                    .getNetworkInterfaces(); networkInterface.hasMoreElements();) {

                NetworkInterface singleInterface = networkInterface.nextElement();

                for (Enumeration<InetAddress> IpAddresses = singleInterface.getInetAddresses(); IpAddresses
                        .hasMoreElements();) {
                    inetAddress = IpAddresses.nextElement();

                    if (!inetAddress.isLoopbackAddress() && (singleInterface.getDisplayName()
                            .contains("wlan0") ||
                            singleInterface.getDisplayName().contains("eth0") ||
                            singleInterface.getDisplayName().contains("ap0"))) {

                        myAddr = inetAddress;
                    }
                }
            }

        } catch (SocketException ex) {
            Log.e("ERROR", ex.toString());
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
        return myAddr;
    }

    public InetAddress getBroadcast(InetAddress inetAddr) {

        if(inetAddr!=null) {
            NetworkInterface temp;
            InetAddress iAddr = null;
            try {
                temp = NetworkInterface.getByInetAddress(inetAddr);
                if(temp!=null) {
                    List<InterfaceAddress> addresses = temp.getInterfaceAddresses();

                    for (InterfaceAddress inetAddress : addresses)

                        iAddr = inetAddress.getBroadcast();
                    Log.d("MSG", "iAddr=" + iAddr);
                    return iAddr;
                } else{
                    return null;
                }

            } catch (SocketException e) {

                e.printStackTrace();
                Log.d("ERROR", "getBroadcast" + e.getMessage());
            }  catch (NullPointerException ex){
                ex.printStackTrace();
            }
        }
        return null;
    }

    public String getCurrentIp() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }

    private InetAddress getBroadcastIp() {
        // Function to return the broadcast address, based on the IP address of the device
        try {

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            //Toast.makeText(context, ""+ipAddress, Toast.LENGTH_SHORT).show();
            String addressString = toBroadcastIp(ipAddress);
            // Toast.makeText(context, "one: "+addressString, Toast.LENGTH_SHORT).show();
            InetAddress broadcastAddress = InetAddress.getByName(addressString);

            //  Toast.makeText(context, "broadcastAddress: "+broadcastAddress, Toast.LENGTH_SHORT).show();
            //  myIp=broadcastAddress.getHostAddress();
            //  Toast.makeText(context, myIp, Toast.LENGTH_SHORT).show();
            return broadcastAddress;
        } catch (UnknownHostException e) {

            Log.e(LOG_TAG, "UnknownHostException in getBroadcastIP: " + e);
            return null;
        } catch (NullPointerException ex){
            ex.printStackTrace();
            return null;
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }

    }

    private String toBroadcastIp(int ip) {
        // Returns converts an IP address in int format to a formatted string
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                "255";
    }
}
