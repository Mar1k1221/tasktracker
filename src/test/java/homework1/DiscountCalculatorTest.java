package homework1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class DiscountCalculatorTest {

   @ParameterizedTest(name="{0} -> проверка обьекта на отрицательное число")
   @ValueSource(ints={-1,-30,-10,-20,-30,-40,-50})
    public void test_For_negative_value(int amount){
       DiscountCalculator discountCalculator = new DiscountCalculator();
        assertThrows(IllegalArgumentException.class,()->discountCalculator.CalculateDiscount(amount) );
   }
   @Test
   @DisplayName("Проверка на 0")
   public void test_For_Zero_Value(){
       DiscountCalculator discountCalculator = new DiscountCalculator();
       int amount = 0;
       assertEquals(amount,discountCalculator.CalculateDiscount(0));

   }
   @Test
   @DisplayName("Проверка на максимальное число.")
   public void maximum_Value_Test(){
       DiscountCalculator discountCalculator = new DiscountCalculator();
       int discount = discountCalculator.CalculateDiscount(50000);
       assertEquals(40000,discount);

   }
   @ParameterizedTest(name = "{0} -> проверка на корректную работоспособность счетчика скидок.")
   @CsvSource({
           "50000,40000",
           "100,100",
           "1100,990",
           "5000,4000",
           "1800,1620"
   })
   public void discount_Test(int a,int b){
       DiscountCalculator discountCalculator = new DiscountCalculator();
       int actual = discountCalculator.CalculateDiscount(a);
       assertEquals(b,actual);
   }


}