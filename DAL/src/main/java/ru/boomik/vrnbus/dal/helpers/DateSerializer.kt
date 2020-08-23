package ru.boomik.vrnbus.dal.helpers


import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

@Serializer(forClass = DateSerializer::class)
object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("DateSerializer", PrimitiveKind.STRING)

    override fun serialize(output: Encoder, obj: Date) {
        output.encodeString(obj.time.toString())
    }

    override fun deserialize(input: Decoder): Date {
        return Date(input.decodeString().toLong())
    }
}