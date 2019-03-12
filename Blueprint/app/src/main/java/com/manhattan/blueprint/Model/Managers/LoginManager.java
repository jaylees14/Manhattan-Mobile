package com.manhattan.blueprint.Model.Managers;

import android.content.Context;

import com.manhattan.blueprint.Model.AccountType;
import com.manhattan.blueprint.Model.DAO.BlueprintDAO;
import com.manhattan.blueprint.Model.Session;

public class LoginManager {
    private Context context;

    public LoginManager(Context context) {
        this.context = context;
    }

    public void login(String username, AccountType accountType) {
        Session session = new Session(username, accountType);
        BlueprintDAO.getInstance(context).setSession(session);
    }

    public boolean isLoggedIn() {
        return BlueprintDAO.getInstance(context).getSession().isPresent();
    }

    public void logout() {
        BlueprintDAO.getInstance(context).clearSession();
    }

    public boolean isDeveloper() {
        return BlueprintDAO.getInstance(context).getSession()
                .bind(Session::getAccountType)
                .withDefault(AccountType.PLAYER)
                .equals(AccountType.DEVELOPER);
    }

    public boolean isLecturer() {
        return BlueprintDAO.getInstance(context).getSession()
                .bind(Session::getAccountType)
                .withDefault(AccountType.PLAYER)
                .equals(AccountType.LECTURER);
    }
}
