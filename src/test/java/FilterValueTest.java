import org.junit.Test;

public class FilterValueTest {

    @Test
    public void matchOnlyDigits(){
        String text = "123.45";
        System.out.println(text.matches("[0-9]+"));
        if (text.matches("[0-9]+")){
            System.out.println("Digits");
        }
    }

    @Test
    public void matchOnlyDouble(){
        String text = "123.45";
        System.out.println(text.matches("[0-9]+\\.[0-9]+"));
        if (text.matches("[0-9]+\\.[0-9]+")){
            System.out.println("Double");
        }
    }
}
