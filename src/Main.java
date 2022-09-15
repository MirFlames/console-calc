import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * @author Dmitry Miroshnikov
 */
public class Main {
    public static void main(String[] args) {
        while (true) {
            try {
                String arithmeticExpression = (new Scanner(System.in)).nextLine();
                String result = calc(arithmeticExpression);
                System.out.println(result);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка в выражении. Калькулятор завершается.");
                return;
            } catch (RuntimeException e) {
                System.out.println("Калькулятор сломался.");
                System.out.println(Arrays.toString(e.getStackTrace()));
                return;
            }
        }
    }

    /**
     *
     * @param input арифмитическое выражение (a + b, a - b, a * b, a / b), допускается использование римской
     *              и арабской форм записи выражения, каждый операнд должен быть в пределах от 1 до 10 включительно.
     * @return результат выполнения операции в цифровой системе, заданной оперендами при записи
     * выражения
     *
     */
    public static String calc(String input) throws IllegalArgumentException {
        String[] operands = new String[2];
        Operand operand1, operand2;
        String result = "";
        String operator = "";

        for (Operator c: Operator.values()) { // мир без регулярок
            int index = input.indexOf(c.toString());
            if (index != -1) {
                operands[0] = input.substring(0, index).trim();
                operands[1] = input.substring(index + 1).trim();
                operator = input.substring(index, index + 1);
            }
        }

        try {
            operand1 = new Operand(operands[0]);
            operand2 = new Operand(operands[1]);
        } catch (InvalidParameterException e) {
            System.out.println("Один из операндов больше 10 или меньше 1.");
            throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            System.out.println("Неверное выражение.");
            throw new IllegalArgumentException();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Выражение введено не польностью.");
            throw new IllegalArgumentException();
        }

        if (operand1.rimNumeric != operand2.rimNumeric) {
            System.out.println("В выражении допустимо использовать только одну числовую систему");
            throw new IllegalArgumentException();
        }

        for (Operator c: Operator.values())
            if (c.toString().equals(operator))
                result = c.execute(operand1, operand2);

        if (operand1.rimNumeric) {
            try {
                return Roman.convertToRoman(result);
            } catch (InvalidAlgorithmParameterException e) {
                System.out.println("Результат выражения меньше 1.");
                throw new IllegalArgumentException();
            }
        }
        else return result;
    }
}


class Operand {
    public int intValue;
    public boolean rimNumeric;

    Operand(String value) throws IllegalArgumentException {
        if (value.equals(""))
            throw new IllegalArgumentException();

        try {
            intValue = Integer.parseInt(value);
            rimNumeric = false;
        } catch (NumberFormatException e) {
            try {
                intValue = Roman.parseInt(value);
                if (!value.equals(Roman.convertToRoman(String.valueOf(intValue)))) {
                    throw new IllegalArgumentException("broken roman number");
                }
                rimNumeric = true;
            } catch (InvalidParameterException x) {
                throw new IllegalArgumentException();
            } catch (InvalidAlgorithmParameterException ex) {
                throw new InvalidParameterException();
            }
        }

        if (intValue > 10 || intValue < 1) {
            throw new InvalidParameterException("operands can take values from 1 to 10"); // требование клиента
        }
    }
}


enum Operator {
    SUM("+") {
        public String execute(Operand operand1, Operand operand2) {
            return Integer.toString(operand1.intValue + operand2.intValue);
        }
    },
    SUB("-") {
        public String execute(Operand operand1, Operand operand2) {
            return Integer.toString(operand1.intValue - operand2.intValue);
        }
    },
    DIV("/") {
        public String execute(Operand operand1, Operand operand2) {
            return Integer.toString(operand1.intValue / operand2.intValue);
        }
    },
    MUL("*") {
        public String execute(Operand operand1, Operand operand2) {
            return Integer.toString(operand1.intValue * operand2.intValue);
        }
    };
    private final String stringOperator;
    Operator(String operator) {
        this.stringOperator = operator;
    }

    public abstract String execute(Operand operand1, Operand operand2);

    @Override
    public String toString() {
        return stringOperator;
    }
}


class Roman {
    private static final String[] romanNumerals = {"I","IV","V","IX","X","XL","L", "XC", "C"};
    private static final int[] arabianNumerals = {1, 4, 5, 9, 10, 40, 50, 90, 100};

    public static int parseInt(String roman) throws InvalidParameterException {
        int result = 0;
        roman = roman.toUpperCase();
        int i = arabianNumerals.length - 1;
        int pos = 0;
        while (i >= 0 && pos < roman.length()) {
            if(roman.length() - pos >= romanNumerals[i].length() && roman.startsWith(romanNumerals[i], pos)) {
                result += arabianNumerals[i];
                pos += romanNumerals[i].length();
            } else {
                i--;
            }
        }
        if (pos == 0 && i == -1) {
            throw new InvalidParameterException("invalid symbol");
        }
        return result;
    }

    public static String convertToRoman(String arabian) throws InvalidAlgorithmParameterException {
        int i = arabianNumerals.length - 1;
        int a = Integer.parseInt(arabian);

        if (a < 1)
            throw new InvalidAlgorithmParameterException("roman number < 1 exception");

        StringBuilder result = new StringBuilder();
        int act;
        while (i >= 0) {
            act = a / arabianNumerals[i];
            a = a % arabianNumerals[i];
            if (act > 0) {
                result.append(romanNumerals[i].repeat(act)); // romanNumerals[i] * act;
            }
            i--;
        }
        return result.toString();
    }
}