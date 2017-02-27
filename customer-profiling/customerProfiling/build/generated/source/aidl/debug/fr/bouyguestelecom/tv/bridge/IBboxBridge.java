/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/rmessara/Documents/projet/bbox-customer-profiling/customer-profiling/customerProfiling/src/main/aidl/fr/bouyguestelecom/tv/bridge/IBboxBridge.aidl
 */
package fr.bouyguestelecom.tv.bridge;
public interface IBboxBridge extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements fr.bouyguestelecom.tv.bridge.IBboxBridge
{
private static final java.lang.String DESCRIPTOR = "fr.bouyguestelecom.tv.bridge.IBboxBridge";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an fr.bouyguestelecom.tv.bridge.IBboxBridge interface,
 * generating a proxy if needed.
 */
public static fr.bouyguestelecom.tv.bridge.IBboxBridge asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof fr.bouyguestelecom.tv.bridge.IBboxBridge))) {
return ((fr.bouyguestelecom.tv.bridge.IBboxBridge)iin);
}
return new fr.bouyguestelecom.tv.bridge.IBboxBridge.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements fr.bouyguestelecom.tv.bridge.IBboxBridge
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
}
}
}
