package de.hpi.cloud.food.fixtures

import de.hpi.cloud.common.couchbase.upsert
import de.hpi.cloud.common.couchbase.withBucket
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.types.L10n
import de.hpi.cloud.food.FOOD_CONTEXT
import de.hpi.cloud.food.entities.Label
import java.net.URI

val labelFixture: List<Label> = listOf(
    Label(
        title = L10n.from(de = "Alkohol", en = "Alcohol"),
        iconUri = URI("https://xml.stw-potsdam.de/images/icons/su_alkohol_a.png")
    ),
    Label(
        title = L10n.from(de = "Knoblauch", en = "Garlic"),
        iconUri = URI("https://xml.stw-potsdam.de/images/icons/su_knoblauch_k_dick.png")
    ),
    Label(
        title = L10n.from(de = "Schwein", en = "Pork"),
        iconUri = URI("https://xml.stw-potsdam.de/images/icons/su_schwein_s.png"),
        aliases = setOf("Schweinefleisch")
    ),
    Label(
        title = L10n.from(de = "Rind", en = "Beef"),
        iconUri = URI("https://xml.stw-potsdam.de/images/icons/su_rind_r.png"),
        aliases = setOf("Rindfleisch")
    ),
    Label(
        title = L10n.from(de = "Lamm", en = "Lamb"),
        iconUri = URI("https://xml.stw-potsdam.de/images/icons/su_lamm_l.png")
    ),
    Label(
        title = L10n.from(de = "Fisch", en = "Fish"),
        iconUri = URI("https://xml.stw-potsdam.de/images/icons/su_fisch_f.png")
    ),
    Label(
        title = L10n.from(de = "Geflügel", en = "Poultry"),
        iconUri = URI("https://xml.stw-potsdam.de/images/icons/su_hahn_g.png"),
        aliases = setOf("Gefluegel", "Huhn", "Hahn", "Hühnchen", "Huehnchen", "Ente", "Gans", "Truthahn")
    ),
    Label(
        title = L10n.from(de = "Wild", en = "Venison"),
        iconUri = URI("https://xml.stw-potsdam.de/images/icons/su_wild_h.png"),
        aliases = setOf("Wildfleisch")
    ),
    Label(
        title = L10n.from(de = "Vegan", en = "Vegan"),
        iconUri = URI("https://xml.stw-potsdam.de/images/icons/su_vegan_w.png")
    ),
    Label(
        title = L10n.from(de = "Vegetarisch", en = "Vegetarian"),
        iconUri = URI("https://xml.stw-potsdam.de/images/icons/su_vegetarisch_v.png")
    ),
    Label(
        title = L10n.from(de = "Vital", en = "Vital"),
        iconUri = URI("https://xml.stw-potsdam.de/images/icons/su_vital_m.png")
    ),
    Label(
        title = L10n.from(de = "Regional", en = "Regional"),
        iconUri = URI("https://xml.stw-potsdam.de/images/icons/su_regional.png")
    ),
    Label(
        title = L10n.from(de = "Draußen", en = "Outdoor"),
        iconUri = URI("https://xml.stw-potsdam.de/images/icons/su_outdoor_o.png"),
        aliases = setOf("Draussen")
    ),
    Label(
        title = L10n.from(de = "Foodtruck", en = "Food Truck"),
        iconUri = URI("https://xml.stw-potsdam.de/images/icons/su_foottruck_o.png")
    )
)

val Label.wrapper : Wrapper<Label>
    get() = createNewWrapper(FOOD_CONTEXT, id)

/**
 * Updates the labels in the database
 */
fun main() {
    withBucket("food") { bucket ->
        labelFixture.forEach { label ->
            bucket.upsert(label.wrapper)
        }
    }
}
