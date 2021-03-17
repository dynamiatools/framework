/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.domain;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import static tools.dynamia.domain.util.DomainUtils.lookupCrudService;

/**
 * Represent a data Entity class with ID and properties
 *
 * @param <ID> the generic type
 * @author Ing. Mario Serrano Leones
 */
public abstract class AbstractEntity<ID extends Serializable> implements Serializable, Identifiable<ID>,
        PropertyChangeListenerContainer, Referenceable<ID>, Jsonable, Xmlable, Mappable {


    private static final long serialVersionUID = 1L;

    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public abstract ID getId();

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    @Override
    public abstract void setId(ID id);

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractEntity other = (AbstractEntity) obj;
        if (this.getId() == null && this != other) {
            return false;
        }

        return this.getId() == other.getId() || (this.getId() != null && this.getId().equals(other.getId()));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.getId() != null ? this.getId().hashCode() : 0);
        return hash;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getName() + "[" + getId() + "]";
    }

    /**
     * Add a PropertyChangeListener to get object change, subclasses must invoke
     * notifyChange to fire listeners
     *
     * @param listener
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener
     *
     * @param listener
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Notify PropertyChangeListeners change, this method automatically check if the
     * oldValue and newValue are different to fire the listeners.
     *
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    protected void notifyChange(String propertyName, Object oldValue, Object newValue) {
        if (oldValue == null || oldValue != newValue) {
            propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    @Override
    public EntityReference<ID> toEntityReference() {
        EntityReference<ID> entityReference = new EntityReference<>(getId(), getClass().getName());
        entityReference.setName(toString());
        entityReference.getAttributes().putAll(toMap());
        return entityReference;
    }

    /**
     * Save this instance
     */
    public void save() {
        lookupCrudService().save(this);
    }

    /**
     * Delete this instance
     */
    public void delete() {
        lookupCrudService().delete(this);
    }

}
