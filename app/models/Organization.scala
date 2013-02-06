package models

/**
 * A legal entity that oragnizes an event and offers marketing or hospitality services.
 * It can be a sport club, culture entity or an association
 * 
 * @author Bartosz Jankiewicz
 */
case class Organization( 
    id: Long, 
    name: String,
    description: String,
    address: Address)