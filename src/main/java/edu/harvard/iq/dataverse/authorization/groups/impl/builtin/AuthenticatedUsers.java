package edu.harvard.iq.dataverse.authorization.groups.impl.builtin;

import edu.harvard.iq.dataverse.authorization.RoleAssignee;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.authorization.RoleAssigneeDisplayInfo;
import edu.harvard.iq.dataverse.authorization.groups.Group;
import edu.harvard.iq.dataverse.authorization.groups.GroupProvider;

public class AuthenticatedUsers implements Group {

    private static final AuthenticatedUsers instance = new AuthenticatedUsers();
    
    private AuthenticatedUsers() {
    }
    
    public static AuthenticatedUsers get() { return instance; }
    
    @Override
    public boolean contains(RoleAssignee ra) {
        return (ra instanceof AuthenticatedUser);
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public GroupProvider getGroupProvider() {
        return BuiltInGroupsProvider.get();
    }

    @Override
    public String getIdentifier() {
        return ":authenticated-users";
    }

    @Override
    public RoleAssigneeDisplayInfo getDisplayInfo() {
        return new RoleAssigneeDisplayInfo("Anyone with a Dataverse account", null);
    }

    @Override
    public String getAlias() {
        return getGroupProvider().getGroupProviderAlias() + Group.PATH_SEPARATOR + "authenticated-users";
    }

    @Override
    public String getDisplayName() {
        return "Authenticated Users";
    }

    @Override
    public String getDescription() {
        return "All users, except for guests";
    }
    
    @Override
    public String toString() {
        return "[AuthenticatedUsers " + getIdentifier() + "]";
    }
}
