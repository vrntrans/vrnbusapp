/*
 * Anonymous API
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: v1
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
package ru.boomik.vrnbus.dal.dto

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.serialization.Serializable
import java.io.IOException
import java.util.*

/**
 * NewRouteWithStationsDto
 */
@Serializable
class NewRouteWithStationsDto {
    /**
     * Get description
     *
     * @return description
     */
    @SerializedName("description")
    var description: String? = null

    /**
     * Get changeDescription
     *
     * @return changeDescription
     */
    @SerializedName("changeDescription")
    var changeDescription: String? = null

    /**
     * Get numberOfBuses
     *
     * @return numberOfBuses
     */
    @SerializedName("numberOfBuses")
    var numberOfBuses: String? = null

    /**
     * Get intervalOfBuses
     *
     * @return intervalOfBuses
     */
    @SerializedName("intervalOfBuses")
    var intervalOfBuses: String? = null

    /**
     * Get newRouteStatus
     *
     * @return newRouteStatus
     */
    @SerializedName("newRouteStatus")
    var newRouteStatus: NamedEntityDto? = null

    /**
     * Get newRouteBusType
     *
     * @return newRouteBusType
     */
    @SerializedName("newRouteBusType")
    var newRouteBusType: NamedEntityDto? = null

    /**
     * Get id
     *
     * @return id
     */
    @SerializedName("id")
    var id: Int? = null

    /**
     * Get name
     *
     * @return name
     */
    @SerializedName("name")
    var name: String? = null

    /**
     * Get type
     *
     * @return type
     */
    @SerializedName("type")
    val type: TypeEnum? = null

    @SerializedName("forwardDirectionStations")
    private var forwardDirectionStations: MutableList<Int>? = null

    @SerializedName("backDirectionStations")
    private var backDirectionStations: MutableList<Int>? = null
    fun description(description: String?): NewRouteWithStationsDto {
        this.description = description
        return this
    }

    fun changeDescription(changeDescription: String?): NewRouteWithStationsDto {
        this.changeDescription = changeDescription
        return this
    }

    fun numberOfBuses(numberOfBuses: String?): NewRouteWithStationsDto {
        this.numberOfBuses = numberOfBuses
        return this
    }

    fun intervalOfBuses(intervalOfBuses: String?): NewRouteWithStationsDto {
        this.intervalOfBuses = intervalOfBuses
        return this
    }

    fun newRouteStatus(newRouteStatus: NamedEntityDto?): NewRouteWithStationsDto {
        this.newRouteStatus = newRouteStatus
        return this
    }

    fun newRouteBusType(newRouteBusType: NamedEntityDto?): NewRouteWithStationsDto {
        this.newRouteBusType = newRouteBusType
        return this
    }

    fun id(id: Int?): NewRouteWithStationsDto {
        this.id = id
        return this
    }

    fun name(name: String?): NewRouteWithStationsDto {
        this.name = name
        return this
    }

    fun forwardDirectionStations(forwardDirectionStations: MutableList<Int>?): NewRouteWithStationsDto {
        this.forwardDirectionStations = forwardDirectionStations
        return this
    }

    fun addForwardDirectionStationsItem(forwardDirectionStationsItem: Int): NewRouteWithStationsDto {
        if (forwardDirectionStations == null) {
            forwardDirectionStations = ArrayList()
        }
        forwardDirectionStations!!.add(forwardDirectionStationsItem)
        return this
    }

    /**
     * Get forwardDirectionStations
     *
     * @return forwardDirectionStations
     */
    fun getForwardDirectionStations(): List<Int>? {
        return forwardDirectionStations
    }

    fun setForwardDirectionStations(forwardDirectionStations: MutableList<Int>?) {
        this.forwardDirectionStations = forwardDirectionStations
    }

    fun backDirectionStations(backDirectionStations: MutableList<Int>?): NewRouteWithStationsDto {
        this.backDirectionStations = backDirectionStations
        return this
    }

    fun addBackDirectionStationsItem(backDirectionStationsItem: Int): NewRouteWithStationsDto {
        if (backDirectionStations == null) {
            backDirectionStations = ArrayList()
        }
        backDirectionStations!!.add(backDirectionStationsItem)
        return this
    }

    /**
     * Get backDirectionStations
     *
     * @return backDirectionStations
     */
    fun getBackDirectionStations(): List<Int>? {
        return backDirectionStations
    }

    fun setBackDirectionStations(backDirectionStations: MutableList<Int>?) {
        this.backDirectionStations = backDirectionStations
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val newRouteWithStationsDto = o as NewRouteWithStationsDto
        return description == newRouteWithStationsDto.description &&
                changeDescription == newRouteWithStationsDto.changeDescription &&
                numberOfBuses == newRouteWithStationsDto.numberOfBuses &&
                intervalOfBuses == newRouteWithStationsDto.intervalOfBuses &&
                newRouteStatus == newRouteWithStationsDto.newRouteStatus &&
                newRouteBusType == newRouteWithStationsDto.newRouteBusType &&
                id == newRouteWithStationsDto.id &&
                name == newRouteWithStationsDto.name &&
                type == newRouteWithStationsDto.type &&
                forwardDirectionStations == newRouteWithStationsDto.forwardDirectionStations &&
                backDirectionStations == newRouteWithStationsDto.backDirectionStations
    }

    override fun hashCode(): Int {
        return Objects.hash(description, changeDescription, numberOfBuses, intervalOfBuses, newRouteStatus, newRouteBusType, id, name, type, forwardDirectionStations, backDirectionStations)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("class NewRouteWithStationsDto {\n")
        sb.append("    description: ").append(toIndentedString(description)).append("\n")
        sb.append("    changeDescription: ").append(toIndentedString(changeDescription)).append("\n")
        sb.append("    numberOfBuses: ").append(toIndentedString(numberOfBuses)).append("\n")
        sb.append("    intervalOfBuses: ").append(toIndentedString(intervalOfBuses)).append("\n")
        sb.append("    newRouteStatus: ").append(toIndentedString(newRouteStatus)).append("\n")
        sb.append("    newRouteBusType: ").append(toIndentedString(newRouteBusType)).append("\n")
        sb.append("    id: ").append(toIndentedString(id)).append("\n")
        sb.append("    name: ").append(toIndentedString(name)).append("\n")
        sb.append("    type: ").append(toIndentedString(type)).append("\n")
        sb.append("    forwardDirectionStations: ").append(toIndentedString(forwardDirectionStations)).append("\n")
        sb.append("    backDirectionStations: ").append(toIndentedString(backDirectionStations)).append("\n")
        sb.append("}")
        return sb.toString()
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private fun toIndentedString(o: Any?): String {
        return o?.toString()?.replace("\n", "\n    ") ?: "null"
    }

    /**
     * Gets or Sets type
     */
    @JsonAdapter(TypeEnum.Adapter::class)
    enum class TypeEnum(val value: Int) {
        NUMBER_1(1), NUMBER_2(2);

        override fun toString(): String {
            return value.toString()
        }

        class Adapter : TypeAdapter<TypeEnum?>() {
            @Throws(IOException::class)
            override fun write(jsonWriter: JsonWriter, enumeration: TypeEnum?) {
                jsonWriter.value(enumeration!!.value)
            }

            @Throws(IOException::class)
            override fun read(jsonReader: JsonReader): TypeEnum? {
                val value = jsonReader.nextInt()
                return fromValue(value.toString())
            }
        }

        companion object {
            fun fromValue(text: String): TypeEnum? {
                for (b in values()) {
                    if (b.value.toString() == text) {
                        return b
                    }
                }
                return null
            }
        }
    }
}