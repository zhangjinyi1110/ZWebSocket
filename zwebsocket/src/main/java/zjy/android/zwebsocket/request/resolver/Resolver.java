package zjy.android.zwebsocket.request.resolver;

import zjy.android.zwebsocket.request.Request;

public interface Resolver {

    void resolve(Chain chain);

    interface Chain {

        Request request();

        String url();

        String residue();

        void setResidue(String residue);

        void proceed();

    }

    static int charMinIndex(String target, char[] chars) {
        int len = target.length();
        for (int i = 0; i < len; i++) {
            char t = target.charAt(i);
            for (char c : chars) {
                if (c == t) return i;
            }
        }
        return -1;
    }

}