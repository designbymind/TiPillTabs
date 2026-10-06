// TiPillTabs: one accepted entry of the `items` property.
// MIT license. See the LICENSE file.

package ti.pilltabs;

import java.util.HashMap;
import java.util.Map;

final class PillItem
{
	final String id;
	final String title;
	// A copy of the dictionary from JavaScript. The `items` getter returns it, with later badge updates.
	final HashMap<String, Object> properties;

	private PillItem(String id, String title, HashMap<String, Object> properties)
	{
		this.id = id;
		this.title = title;
		this.properties = properties;
	}

	// Returns null when the entry is not a dictionary with a nonempty string `id` and a string `title`.
	// `systemImage` is an SF Symbol name. Android has no SF Symbols, so the key is not required here.
	// The Android icon comes from the optional keys `icon` with `iconFamily`, or `image`.
	static PillItem from(Object entry)
	{
		if (!(entry instanceof Map)) {
			return null;
		}
		HashMap<String, Object> properties = new HashMap<>();
		for (Map.Entry<?, ?> pair : ((Map<?, ?>) entry).entrySet()) {
			if (pair.getKey() instanceof String) {
				properties.put((String) pair.getKey(), pair.getValue());
			}
		}
		Object id = properties.get("id");
		Object title = properties.get("title");
		if (!(id instanceof String) || ((String) id).isEmpty() || !(title instanceof String)) {
			return null;
		}
		return new PillItem((String) id, (String) title, properties);
	}
}
