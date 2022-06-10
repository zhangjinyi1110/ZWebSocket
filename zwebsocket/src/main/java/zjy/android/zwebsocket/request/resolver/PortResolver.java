package zjy.android.zwebsocket.request.resolver;

public class PortResolver implements Resolver {
    @Override
    public void resolve(Chain chain) {
        String temp = chain.residue();
        if (temp.length() == 0) {
            defPort(chain);
        } else {
            int index = temp.indexOf(':');
            if (index == -1) {
                defPort(chain);
            } else {
                int minIndex = Resolver.charMinIndex(temp, new char[]{'/', '?', '&', '#'});
                if (minIndex == -1) {
                    chain.request().setPort(Integer.parseInt(temp.substring(1)));
                    chain.setResidue("");
                } else if (minIndex > index) {
                    chain.request().setPort(Integer.parseInt(temp.substring(1, minIndex)));
                    chain.setResidue(temp.substring(minIndex));
                } else {
                    defPort(chain);
                }
            }
        }
        chain.proceed();
    }

    private void defPort(Chain chain) {
        if (chain.url().startsWith("http://")) {
            chain.request().setPort(80);
        } else if (chain.url().startsWith("https://")) {
            chain.request().setPort(443);
        } else {
            throw new IllegalArgumentException("utl protocol must is 'http' or 'https', curr url is " + chain.url());
        }
    }
}
