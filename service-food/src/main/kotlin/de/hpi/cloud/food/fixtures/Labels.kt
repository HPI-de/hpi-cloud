package de.hpi.cloud.food.fixtures

import de.hpi.cloud.common.couchbase.upsert
import de.hpi.cloud.common.couchbase.withBucket
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.types.L10n
import de.hpi.cloud.food.FOOD_CONTEXT
import de.hpi.cloud.food.entities.Label
import java.net.URL

val labelFixture: List<Label> = listOf(
    Label(
        title = L10n.from(de = "Alkohol", en = "Alcohol"),
        iconUrl = URL("https://xml.stw-potsdam.de/images/icons/su_alkohol_a.png"),
        aliases = setOf("Alkohol")
    ),
    Label(
        title = L10n.from(de = "Rind", en = "Beef"),
        iconUrl = URL("https://xml.stw-potsdam.de/images/icons/su_rind_r.png"),
        aliases = setOf("Rindfleisch")
    ),
    Label(
        title = L10n.from(de = "Fisch", en = "Fish"),
        iconUrl = URL("https://xml.stw-potsdam.de/images/icons/su_fisch_f.png"),
        aliases = setOf("Fisch")
    ),
    Label(
        title = L10n.from(de = "Knoblauch", en = "Garlic"),
        iconUrl = URL("https://xml.stw-potsdam.de/images/icons/su_knoblauch_k_dick.png"),
        aliases = setOf("Knoblauch")
    ),
    Label(
        title = L10n.from(de = "Lamm", en = "Lamb"),
        iconUrl = URL("https://xml.stw-potsdam.de/images/icons/su_lamm_l.png"),
        aliases = setOf("Lamm")
    ),
    Label(
        title = L10n.from(de = "Schwein", en = "Pork"),
        iconUrl = URL("https://xml.stw-potsdam.de/images/icons/su_schwein_s.png"),
        aliases = setOf("Schweinefleisch")
    ),
    Label(
        title = L10n.from(de = "Geflügel", en = "Poultry"),
        iconUrl = URL("https://xml.stw-potsdam.de/images/icons/su_hahn_g.png"),
        aliases = setOf("Gefluegel")
    ),
    Label(
        title = L10n.from(de = "Vegan", en = "Vegan"),
        iconUrl = URL("https://xml.stw-potsdam.de/images/icons/su_vegan_w.png"),
        aliases = setOf("Vegan")
    ),
    Label(
        title = L10n.from(de = "Vegetarisch", en = "Vegetarian"),
        iconUrl = URL("https://xml.stw-potsdam.de/images/icons/su_vegetarisch_v.png"),
        aliases = setOf("Vegetarisch")
    ),
    Label(
        title = L10n.from(de = "Vital", en = "Vital"),
        iconUrl = URL("https://xml.stw-potsdam.de/images/icons/su_vital_m.png"),
        aliases = setOf("Vital")
    ),
    Label(
        title = L10n.from(de = "Wild", en = "Venison"),
        iconUrl = URL("https://xml.stw-potsdam.de/images/icons/su_wild_h.png"),
        aliases = setOf("Wildfleisch")
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
