package com.d.stalker;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionInitializer;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

public class MinaClient extends IoHandlerAdapter implements Runnable{
	final String TAG="MinaClient";
	final String ipAddress="221.236.156.107";
	final int port=9123;
	public  IoSession session;
	Context context;
	public MinaClient(Context cx){
		context=cx;
		
	}
	public void connection(){
		try{
		NioSocketConnector connector = new NioSocketConnector();
		connector.getFilterChain().addLast( "logger", new LoggingFilter() ); 
		connector.getFilterChain().addLast( "codec", new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" )))); //���ñ�������� 
		connector.setConnectTimeoutMillis(1000*60);
		connector.setHandler(this);
		ConnectFuture cf = connector.connect(new InetSocketAddress(ipAddress, port));//�������� 
		
		cf.awaitUninterruptibly();//�ȴ����Ӵ������ 
		session=cf.getSession();
		
		Message  message=new Message();
		message.setFormUser("dawn");
		message.setMessage("��¼");
		message.setFlag("login");//�״ε�¼
		session.write(JSON.toJSONString(message));
		
		session.getCloseFuture().awaitUninterruptibly();//�ȴ����ӶϿ� 
		connector.dispose();
		System.out.println("ͨ�Ž���!");
		}catch(Exception e){
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
			
		}
	}
	
	public  void sendMessage(final Message message){
		if(session!=null){
			Log.i(TAG, "isConnected:"+session.isConnected());
			message.setFormUser("dawn");
		   new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(!session.isConnected()){
					connection();
				}
				 session.write(JSON.toJSONString(message));
				
			}
		}).start();
		}else{
			connection();
			Log.i(TAG, "sendMessage->session is null ,���ڳ�����������!");
			Toast.makeText(context, "û�п�������!", Toast.LENGTH_SHORT).show();
		}
		
	}
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		 Message msg=JSON.parseObject(message.toString(), Message.class); 
		 System.out.println("�յ���Ϣ��"+msg.getMessage());
		super.messageReceived(session, message);
	}
	@Override
	public void run() {
		connection();
		
	}

}
