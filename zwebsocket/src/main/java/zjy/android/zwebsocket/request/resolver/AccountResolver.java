package zjy.android.zwebsocket.request.resolver;

public class AccountResolver implements Resolver {
    @Override
    public void resolve(Chain chain) {
        String temp = chain.residue();
        int end = temp.indexOf('@');
        if (end != -1) {
            int minIndex = Resolver.charMinIndex(temp, new char[]{'/', '?', '&', '#'});
            if (minIndex == -1 || minIndex > end) {
                String str = temp.substring(0, end);
                String[] account = str.split(":");
                if (account.length != 2) {
                    throw new IllegalArgumentException("username and password is 'username=password', curr arg is " + str);
                }
                chain.request().setUsername(account[0]);
                chain.request().setPassword(account[1]);
                chain.setResidue(temp.substring(end + 1));
            }
        }
        chain.proceed();
    }
}
