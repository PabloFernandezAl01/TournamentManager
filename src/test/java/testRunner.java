import com.intuit.karate.junit5.Karate;

class testRunner {
    
    @Karate.Test
    Karate testLogin() {
        return Karate.run("principal").relativeTo(getClass());
    }    
}
