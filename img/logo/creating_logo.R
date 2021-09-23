# remotes::install_github("GuangchuangYu/hexSticker", force = TRUE)

library(hexSticker)
library(ggplot2)
library(glue)
library(magick)
library(fs)
library(showtext)

## Loading Google fonts (http://www.google.com/fonts)
font_add_google("Love Ya Like A Sister", "rock")
## Automatically use showtext to render text for future devices
showtext_auto()

img <- image_read(path(getwd(), "img", "logo", "staple_logo.png"))
logo <- image_ggplot(img, interpolate = TRUE)

sticker(
  logo,
  package = "staplr",
  p_size = 25,
  s_width = 0.6,
  s_height = 0.6,
  s_x = 1.0,
  s_y = 0.84,
  h_fill =  "#FFE8C6",
  h_color = "#F12709",
  p_color = "#2c3e50",
  p_family = "rock",
  h_size = 2.4,
  white_around_sticker = T,
  filename = path(getwd(), "img", "logo", "logo.png"),
  url = "https://pridiltal.github.io/staplr/",
  u_family = "rock",
  u_size = 4.7,
  spotlight = T,
  l_alpha = 0.5,
  dpi = 300,
  u_color = "#0F2536"
)

fs::file_delete(fs::path(getwd(), "logo.png"))

fs::file_copy(
  path = path(getwd(), "img", "logo", "logo.png"),
  new_path = path(getwd(), "logo.png")
)