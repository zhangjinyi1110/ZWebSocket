package zjy.android.websocketapp;

import zjy.android.websocketapp.request.Request;

public class RequestMain {

    public static void main(String[] args) {
        Request request = Request.newBuilder().setUrl("wss://zhang:hhh@www.baidu" +
                ".com:124/index/index/index?a=b&c=d&e=f#fragment").build();
        System.out.println(request.httpUrl.toString());
        request = Request.newBuilder().setUrl("wss://www.baidu.com:124/index/index/index?a=b&c=d" +
                "&e=f#fragment").build();
        System.out.println(request.httpUrl.toString());
        request = Request.newBuilder().setUrl("wss://zhang:hhh@www.baidu" +
                ".com:124/index/index/index?a=b&c=d&e=f").build();
        System.out.println(request.httpUrl.toString());
        request = Request.newBuilder().setUrl("wss://zhang:hhh@www.baidu" +
                ".com:124/index/index/index#fragment").build();
        System.out.println(request.httpUrl.toString());
        request = Request.newBuilder().setUrl("wss://zhang:hhh@www.baidu" +
                ".com:124?a=b&c=d&e=f#fragment").build();
        System.out.println(request.httpUrl.toString());
        request = Request.newBuilder().setUrl("wss://zhang:hhh@www.baidu" +
                ".com#fragment").build();
        System.out.println(request.httpUrl.toString());
    }

}
