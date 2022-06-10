package edu.iis.mto.testreactor.atm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import edu.iis.mto.testreactor.atm.bank.AccountException;
import edu.iis.mto.testreactor.atm.bank.AuthorizationException;
import edu.iis.mto.testreactor.atm.bank.Bank;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Currency;
import java.util.LinkedList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ATMachineTest {
    @Mock
    private Bank bank;

    private ATMachine atMachine;

    @Test
    public void withdrawTest() throws ATMOperationException {
        // Given
        atMachine = ATMachine.of(bank, Currency.getInstance("PLN"));
        List<BanknotesPack> banknotesPackList = new LinkedList<>();
        banknotesPackList.add(BanknotesPack.create(10, Banknote.PL_10));

        MoneyDeposit deposit = MoneyDeposit.of(Currency.getInstance("PLN"), banknotesPackList);
        atMachine.setDeposit(deposit);

        PinCode pin = PinCode.createPIN(1, 2, 3, 4);
        Card card = Card.create("qwe");
        Money amount = new Money(10, Money.DEFAULT_CURRENCY);

        // When
        Withdrawal withdrawal = atMachine.withdraw(pin, card, amount);
        //Then
        assertEquals(1, withdrawal.getBanknotes().size());
    }

    @Test
    public void getCurrentDepositTest() {
        //Given
        atMachine = ATMachine.of(bank, Currency.getInstance("PLN"));
        List<BanknotesPack> banknotesPackList = new LinkedList<>();
        banknotesPackList.add(BanknotesPack.create(10, Banknote.PL_10));

        MoneyDeposit deposit = MoneyDeposit.of(Currency.getInstance("PLN"), banknotesPackList);
        atMachine.setDeposit(deposit);

        PinCode pin = PinCode.createPIN(1, 2, 3, 4);
        Card card = Card.create("qwe");
        Money amount = new Money(10, Money.DEFAULT_CURRENCY);
        //When
        MoneyDeposit newDeposit = atMachine.getCurrentDeposit();
        // Then
        assertEquals(1, newDeposit.getBanknotes().size());

    }

    @Test
    public void notEnoughBanknotesTest() throws ATMOperationException {
        // Given
        atMachine = ATMachine.of(bank, Currency.getInstance("PLN"));
        List<BanknotesPack> banknotesPackList = new LinkedList<>();
        banknotesPackList.add(BanknotesPack.create(20, Banknote.PL_10));

        MoneyDeposit deposit = MoneyDeposit.of(Currency.getInstance("PLN"), banknotesPackList);
        atMachine.setDeposit(deposit);

        PinCode pin = PinCode.createPIN(1, 2, 3, 4);
        Card card = Card.create("qwe");
        Money amount = new Money(20, Money.DEFAULT_CURRENCY);

        // When
        Withdrawal withdrawal = atMachine.withdraw(pin, card, amount);
        //Then
        assertEquals(10, withdrawal.getBanknotes().get(0).getDenomination());
        assertEquals(2, withdrawal.getBanknotes().size());
    }

    @Test
    public void noFundsOnAccountTest() throws AccountException, ATMOperationException {
        // Given
        atMachine = ATMachine.of(bank, Currency.getInstance("PLN"));
        List<BanknotesPack> banknotesPackList = new LinkedList<>();
        banknotesPackList.add(BanknotesPack.create(20, Banknote.PL_10));

        MoneyDeposit deposit = MoneyDeposit.of(Currency.getInstance("PLN"), banknotesPackList);
        atMachine.setDeposit(deposit);

        PinCode pin = PinCode.createPIN(1, 2, 3, 4);
        Card card = Card.create("qwe");
        Money amount = new Money(20, Money.DEFAULT_CURRENCY);

        Mockito.doThrow(AccountException.class).when(bank).charge(Mockito.any(), Mockito.any());


        // When
//        Withdrawal withdrawal = atMachine.withdraw(pin, card, amount);

        //Then
        try {
            atMachine.withdraw(pin, card, amount);
        } catch (ATMOperationException e) {
            assertEquals(e.getErrorCode(), ErrorCode.NO_FUNDS_ON_ACCOUNT);
        }

    }


    @Test
    public void authorizationErrorTest() throws AuthorizationException {
        // Given
        atMachine = ATMachine.of(bank, Currency.getInstance("PLN"));
        List<BanknotesPack> banknotesPackList = new LinkedList<>();
        banknotesPackList.add(BanknotesPack.create(20, Banknote.PL_10));

        MoneyDeposit deposit = MoneyDeposit.of(Currency.getInstance("PLN"), banknotesPackList);
        atMachine.setDeposit(deposit);

        PinCode pin = PinCode.createPIN(1, 2, 3, 4);
        Card card = Card.create("qwe");
        Money amount = new Money(20, Money.DEFAULT_CURRENCY);

        Mockito.doThrow(AuthorizationException.class).when(bank).autorize(Mockito.any(), Mockito.any());


        //Then
        try {
            atMachine.withdraw(pin, card, amount);
        } catch (ATMOperationException e) {
            assertEquals(e.getErrorCode(), ErrorCode.AUTHORIZATION);
        }
    }

    @Test
    public void unavailableCurrencyTest() {
        // Given
        atMachine = ATMachine.of(bank, Currency.getInstance("PLN"));
        List<BanknotesPack> banknotesPackList = new LinkedList<>();
        banknotesPackList.add(BanknotesPack.create(20, Banknote.PL_10));

        MoneyDeposit deposit = MoneyDeposit.of(Currency.getInstance("USD"), banknotesPackList);
        atMachine.setDeposit(deposit);

        PinCode pin = PinCode.createPIN(1, 2, 3, 4);
        Card card = Card.create("qwe");
        Money amount = new Money(20, Money.DEFAULT_CURRENCY);

        try {
            atMachine.withdraw(pin, card, amount);
        } catch (ATMOperationException e) {
            assertEquals(e.getErrorCode(), ErrorCode.WRONG_CURRENCY);
        }
    }

    @Test
    public void cannotWithdrawTest() {
        // Given
        atMachine = ATMachine.of(bank, Currency.getInstance("PLN"));

        PinCode pin = PinCode.createPIN(1, 2, 3, 4);
        Card card = Card.create("qwe");
        Money amount = new Money(10, Money.DEFAULT_CURRENCY);

        try {
            atMachine.withdraw(pin, card, amount);
        } catch (ATMOperationException e) {
            assertEquals(e.getErrorCode(), ErrorCode.WRONG_AMOUNT);
        }
    }

    @Test
    public void wrongAmountToWithdrawTest() {
        // Given
        atMachine = ATMachine.of(bank, Currency.getInstance("PLN"));
        List<BanknotesPack> banknotesPackList = new LinkedList<>();
        banknotesPackList.add(BanknotesPack.create(10, Banknote.PL_10));

        MoneyDeposit deposit = MoneyDeposit.of(Currency.getInstance("PLN"), banknotesPackList);
        atMachine.setDeposit(deposit);

        PinCode pin = PinCode.createPIN(1, 2, 3, 4);
        Card card = Card.create("qwe");
        Money amount = new Money(3.1234, Money.DEFAULT_CURRENCY);

        try {
            atMachine.withdraw(pin, card, amount);
        } catch (ATMOperationException e) {
            assertEquals(e.getErrorCode(), ErrorCode.WRONG_AMOUNT);
        }
    }

}
