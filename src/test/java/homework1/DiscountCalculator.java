package homework1;

public class DiscountCalculator {
    public int CalculateDiscount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Сумма меньше 0, некорректные параметры.");
        }
        if (amount > 2000) {
            int discountAmount = amount * 20 / 100;
            return amount - discountAmount;
        } else if (amount > 1000) {
            int discountAmount = amount * 10 / 100;
            return amount - discountAmount;
        }else{
            return amount;
        }

    }
}
