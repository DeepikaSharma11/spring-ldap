package org.springframework.ldap.support.transaction;

import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapRdn;
import org.springframework.ldap.core.LdapRdnComponent;

/**
 * A {@link TempEntryRenamingStrategy} that moves the entry to a different
 * subtree than the original entry. The specified subtree needs to be present in
 * the LDAP tree; it will not be created and operations using this strategy will
 * fail if it is not in place. However, this strategy is preferrable to
 * {@link DefaultTempEntryRenamingStrategy}, as it makes it possible to have
 * searches have the expected result even though the temporary entry still exits
 * until the end of the transaction.
 * <p>
 * Example: If the specified <code>subtreeNode</code> is
 * <code>ou=tempEntries</code> and the <code>originalName</code> is
 * <code>cn=john doe, ou=company1, c=SE</code>, the result of
 * {@link #getTemporaryName(Name)} will be
 * <code>cn=john doe1, ou=tempEntries</code>. The &quot;1&quot; suffix is a
 * sequence number needed to prevent potential collisions in the temporary
 * storage.
 * 
 * @author Mattias Arthursson
 * 
 */
public class DifferentSubtreeTempEntryRenamingStrategy implements
        TempEntryRenamingStrategy {

    private Name subtreeNode;

    private static int nextSequenceNo = 1;

    public DifferentSubtreeTempEntryRenamingStrategy(Name subtreeNode) {
        this.subtreeNode = subtreeNode;
    }

    public Name getSubtreeNode() {
        return subtreeNode;
    }

    public void setSubtreeNode(Name subtreeNode) {
        this.subtreeNode = subtreeNode;
    }

    int getNextSequenceNo() {
        return nextSequenceNo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.TempEntryRenamingStrategy#getTemporaryName(javax.naming.Name)
     */
    public Name getTemporaryName(Name originalName) {
        DistinguishedName tempName = new DistinguishedName(originalName);
        List names = tempName.getNames();
        LdapRdn rdn = (LdapRdn) names.get(names.size() - 1);
        LdapRdnComponent component = rdn.getComponent();

        LdapRdn newRdn;
        synchronized (this) {
            newRdn = new LdapRdn(component.getKey(), component.getValue()
                    + nextSequenceNo++);
        }

        DistinguishedName newName = new DistinguishedName(subtreeNode);
        newName.add(newRdn);
        return newName;
    }
}
