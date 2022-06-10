package zjy.android.zwebsocket.request.resolver;

public class HostResolver implements Resolver {
    @Override
    public void resolve(Chain chain) {
        String temp = chain.residue();
        if (temp.length() == 0) {
            throw new IllegalArgumentException("curr url have not host, url is " + chain.url());
        }
        int index = temp.indexOf(":");
        int minIndex = Resolver.charMinIndex(temp, new char[]{'/', '?', '&', '#'});
//        if (index == -1) {
//            if (minIndex == -1) {
//                chain.request().setHost(temp);
//                chain.setResidue("");
//            } else {
//                chain.request().setHost(temp.substring(0, minIndex));
//                chain.setResidue(temp.substring(minIndex));
//            }
//        } else {
//            if (minIndex == -1) {
//                chain.request().setHost(temp.substring(0, index));
//                chain.setResidue(temp.substring(index));
//            } else {
//
//            }
//        }
        if (index == -1 && minIndex == -1) {
            chain.request().setHost(temp);
            chain.setResidue("");
        } /*else if (index == -1) {
            chain.request().setHost(temp.substring(0, minIndex));
            chain.setResidue(temp.substring(minIndex));
        } */ else if (minIndex == -1 || minIndex > index) {
            chain.request().setHost(temp.substring(0, index));
            chain.setResidue(temp.substring(index));
        } else {
            chain.request().setHost(temp.substring(0, minIndex));
            chain.setResidue(temp.substring(minIndex));
        }
        chain.proceed();
    }
}
