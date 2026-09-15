package homework1;

public class StringParser {
    public void parse(String text){
       if ( text == null){
           throw new IllegalArgumentException("Строка равна  null.");
       }
       if (text.isBlank()){
           throw new IllegalArgumentException("Строка является пустой или состоит из пробелов ");
       }
    }
}
