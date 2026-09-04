# What changed in the UI, and why

## The bug behind the uneven buttons

Under a **Material3 theme**, a plain `<Button>` tag in a layout is silently
swapped for a `MaterialButton` at inflation time. You never see this happen,
but MaterialButton behaves differently in two ways that matter:

1. **It ignores `android:background`.** It paints itself using
   `app:backgroundTint` instead. So `android:background="@drawable/bg_header_button"`
   simply never drew — which is why Favourite / History / Orders looked like
   floating white text with no pill behind them.

2. **It adds a hidden 6dp inset** above and below itself. So
   `android:layout_height="38dp"` produced roughly 26dp of visible button,
   and any button that sized itself with `wrap_content` ended up a different
   height from its neighbours.

**The fix:** declare `MaterialButton` explicitly, set
`android:insetTop`/`insetBottom` to `0dp`, and use `app:backgroundTint`
rather than `android:background`. All of that now lives in shared styles, so
it is applied once rather than remembered in twenty places.

## New files

### `res/values/dimens.xml`
Every size in the app, named. Spacing follows a 4dp grid
(`space_xs` 4dp → `space_xxl` 28dp), which is the Material Design standard.
If a gap looks wrong, change it here once and every screen follows.

### `res/values/styles.xml`
Named button styles, so buttons cannot drift apart again:

| Style | Use |
|---|---|
| `Lendly.Button` | Main filled dark blue action, 48dp tall |
| `Lendly.Button.Green` | Buy, Accept |
| `Lendly.Button.Red` | Reject |
| `Lendly.Button.Outlined` | Secondary action on white |
| `Lendly.Button.Header` | Translucent pill on the blue header |
| `Lendly.Button.Card` / `.Card.Outlined` | Small buttons inside a request card |
| `Lendly.SectionTitle`, `Lendly.ToolbarTitle`, `Lendly.EmptyText` | Repeated text patterns |

Primary buttons are **48dp**, which is Android's minimum recommended touch
target — worth a sentence in your report under accessibility.

## Screen-by-screen

**Home header** — the three buttons are `width=0dp` + `weight=1`, so they
split the row into exact thirds regardless of label length. Icons added
back. The pill background now actually renders.

**Category row** — fixed `78dp` width and a forced 2-line label, so
"Study Materials" no longer pushes its circle out of line with "Books".
Added `clipToPadding="false"` so the last category scrolls fully into view
instead of being clipped at the screen edge.

**Request cards** — previously five buttons shared one weighted row, so when
three were visible each got a third of the card and labels wrapped. Now
split into two rows of at most two buttons, each exactly half width. The
adapter hides a row entirely when it holds no visible buttons, so there is
no leftover gap.

**Item detail** — Borrow and Buy are equal halves; Buy is green to separate
buying from borrowing at a glance.

**Map** — zoom buttons are exactly square (48×48) with matching corners.

## If you want to keep going

Ranked by effort-to-payoff for your marks:

1. **Empty-state illustrations.** A small icon above "Nothing listed yet"
   looks far more finished than a line of grey text.
2. **Loading spinners.** Right now a slow network shows an empty screen.
   A `ProgressBar` while `Repo` is fetching would fix that.
3. **Pull to refresh.** Wrap the Home RecyclerView in a
   `SwipeRefreshLayout` — roughly ten lines, and it feels much more like a
   real app.
4. **Item detail image carousel.** Multiple photos per listing.
5. **Push notifications** via Firebase Cloud Messaging for return reminders.
   This would directly implement the reminder feature described in section
   3.4 of your proposal, which the current build only does passively via
   the Activity tab.
