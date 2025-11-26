(function ($) {
  "use strict";

  /*----------------------------------
  # header sticky 
  -----------------------------------*/

  var activeSticky = $(".active-sticky"),
    winDow = $(window);
  winDow.on("scroll", function () {
    var scroll = $(window).scrollTop(),
      isSticky = activeSticky;

    if (scroll < 1) {
      isSticky.removeClass("is-sticky");
      $("#scrollUp").removeClass("show");
    } else {
      isSticky.addClass("is-sticky");
      $("#scrollUp").addClass("show");
    }
  });

  /*----------------------------------
  # Off Canvas Menu
  -----------------------------------*/

  var $offcanvasNav = $("#offcanvasNav a");
  $offcanvasNav.on("click", function () {
    var link = $(this);
    var closestUl = link.closest("ul");
    var activeLinks = closestUl.find(".active");
    var closestLi = link.closest("li");
    var linkStatus = closestLi.hasClass("active");
    var count = 0;

    closestUl.find("ul").slideUp(function () {
      if (++count == closestUl.find("ul").length)
        activeLinks.removeClass("active");
    });

    if (!linkStatus) {
      closestLi.children("ul").slideDown();
      closestLi.addClass("active");
    }
  });

  var $offcanvasNav2 = $("#shop-dropdown a");
  $offcanvasNav2.on("click", function () {
    var link = $(this);
    var closestUl = link.closest("ul");
    var activeLinks = closestUl.find(".active");
    var closestLi = link.closest("li");
    var linkStatus = closestLi.hasClass("active");
    var count = 0;

    closestUl.find("ul").slideUp(function () {
      if (++count == closestUl.find("ul").length)
        activeLinks.removeClass("active");
    });

    if (!linkStatus) {
      closestLi.children("ul").slideDown();
      closestLi.addClass("active");
    }
  });

  // dropdownMenu start
  $.fn.dropdownMenu = function (options) {
    var $a = $(this),
      $b = $(this).find(".topbar-nav-link").next();
    $a.on("click", ".topbar-nav-link", function (e) {
      e.stopPropagation();
      var $c = $(this).next().hasClass("topbar-dropdown-menu");
      if ($c === true) {
        e.preventDefault();
      }

      $(this)
        .next()
        .slideToggle(300)
        .parent()
        .siblings()
        .children("ul")
        .not($(this).next())
        .hide();
    }),
      $(document).on("click", $b, function (e) {
        $b.hide(300);
      });
  };

  $(".simple-menu").dropdownMenu();

  // simple-menu end

  $(".search").on("click", (event) => {
    event.stopPropagation();
    event.preventDefault();
    $(".header-serch-form").slideToggle();
  });

  /*-----------------------------------
  # hero-slider
  ------------------------------ */
  var heroSlider = new Swiper(".hero-slider .swiper-container", {
    loop: true,
    speed: 600,
    autoplay: true,
    lazy: true,
    fadeEffect: {
      crossFade: true,
    },
    pagination: {
      el: ".hero-slider .swiper-pagination",
      clickable: true,
    },

    navigation: {
      nextEl: ".hero-slider .swiper-button-next",
      prevEl: ".hero-slider .swiper-button-prev",
    },
  });

  /*-----------------------------------
  # brand-carousel
  ------------------------------ */

  var brandCarousel = new Swiper(".brand-carousel .swiper-container", {
    loop: true,
    speed: 800,
    slidesPerView: 7,
    spaceBetween: 10,
    pagination: false,
    navigation: {
      nextEl: ".brand-carousel .swiper-button-next",
      prevEl: ".brand-carousel .swiper-button-prev",
    },
    // Responsive breakpoints
    breakpoints: {
      // when window width is >= 320px
      0: {
        slidesPerView: 1,
      },

      // when window width is >= 480px
      480: {
        slidesPerView: 3,
      },

      768: {
        slidesPerView: 4,
      },

      // when window width is >= 640px
      992: {
        slidesPerView: 5,
      },
      1200: {
        slidesPerView: 7,
      },
    },
  });

  /*-----------------------------------
  # product tab list carousel
  ------------------------------ */

  var producttablist = new Swiper(".product-tab-list .swiper-container", {
    loop: true,
    slidesPerView: 4,
    spaceBetween: 30,
    pagination: false,
    observer: true,
    observeParents: true,

    navigation: {
      nextEl: ".product-tab-list .swiper-button-next",
      prevEl: ".product-tab-list .swiper-button-prev",
    },
    // Responsive breakpoints
    breakpoints: {
      // when window width is >= 320px
      0: {
        slidesPerView: 1,
        autoplay: {
          delay: 2000,
        },
        speed: 1000,
      },
      // when window width is >= 480px
      480: {
        slidesPerView: 2,
        autoplay: {
          delay: 2000,
        },
        speed: 1000,
      },
      // when window width is >= 640px
      657: {
        slidesPerView: 2,
      },
      992: {
        slidesPerView: 3,
      },

      // when window width is >= 640px
      1200: {
        slidesPerView: 4,
      },
    },
  });
  /*-----------------------------------
  # eatured list carosel
  ------------------------------ */

  var featuredProducttablist = new Swiper(
    ".featured-list-carosel .swiper-container",
    {
      loop: true,
      slidesPerView: 4,
      spaceBetween: 30,
      pagination: false,
      observer: true,
      observeParents: true,

      navigation: {
        nextEl: ".featured-list-carosel .swiper-button-next",
        prevEl: ".featured-list-carosel .swiper-button-prev",
      },
      // Responsive breakpoints
      breakpoints: {
        // when window width is >= 320px
        0: {
          slidesPerView: 1,
          autoplay: {
            delay: 2000,
          },
          speed: 1000,
        },
        // when window width is >= 480px
        480: {
          slidesPerView: 2,
          autoplay: {
            delay: 2000,
          },
          speed: 1000,
        },
        // when window width is >= 640px
        657: {
          slidesPerView: 2,
        },
        992: {
          slidesPerView: 3,
        },

        // when window width is >= 640px
        1200: {
          slidesPerView: 4,
        },
      },
    }
  );
  /*-----------------------------------
  # featured product carousel
  ------------------------------ */

  var featuredProduct = new Swiper(".featured-product .swiper-container", {
    loop: true,
    slidesPerView: 4,
    spaceBetween: 30,
    pagination: false,
    observer: true,
    observeParents: true,

    navigation: {
      nextEl: ".featured-product .swiper-button-next",
      prevEl: ".featured-product .swiper-button-prev",
    },
    // Responsive breakpoints
    breakpoints: {
      // when window width is >= 320px
      0: {
        slidesPerView: 1,
        autoplay: {
          delay: 2000,
        },
        speed: 1000,
      },
      // when window width is >= 480px
      480: {
        slidesPerView: 2,
        autoplay: {
          delay: 2000,
        },
        speed: 1000,
      },
      // when window width is >= 640px
      657: {
        slidesPerView: 2,
      },
      992: {
        slidesPerView: 3,
      },

      // when window width is >= 640px
      1200: {
        slidesPerView: 4,
      },
    },
  });

  /*-----------------------------------
  # product carousel2
  ------------------------------ */

  var decorationActive = new Swiper(
    ".decoration-slider-active .swiper-container",
    {
      slidesPerView: 2,
      centeredSlides: true,
      loop: true,
      pagination: false,
      navigation: {
        nextEl: ".decoration-slider-active .swiper-button-next",
        prevEl: ".decoration-slider-active .swiper-button-prev",
      },

      // Responsive breakpoints
      breakpoints: {
        // when window width is >= 320px
        0: {
          slidesPerView: 1,
          centeredSlides: false,
          autoplay: {
            delay: 2000,
          },
          speed: 1000,
        },
        992: {
          slidesPerView: 2,
          spaceBetween: 30,
        },
        1200: {
          spaceBetween: 125,
        },
      },
    }
  );

  /*-----------------------------------
  # product list carousel
  ------------------------------ */

  var productListCarousel = new Swiper(
    ".product-list-carousel .swiper-container",
    {
      loop: false,
      speed: 800,
      slidesPerView: 4,
      spaceBetween: 15,
      pagination: false,
      navigation: {
        nextEl: ".product-list-carousel .swiper-button-next",
        prevEl: ".product-list-carousel .swiper-button-prev",
      },
      observer: true,
      observeParents: true,
      // Responsive breakpoints
      breakpoints: {
        // when window width is >= 320px
        0: {
          slidesPerView: 1,
          loop: true,
          autoplay: {
            delay: 2000,
          },
          speed: 1000,
        },
        // when window width is >= 480px
        575: {
          slidesPerView: 2,
          loop: true,
          autoplay: {
            delay: 2000,
          },
          speed: 1000,
        },
        // when window width is >= 640px
        657: {
          slidesPerView: 2,
        },
        992: {
          slidesPerView: 2,
        },

        // when window width is >= 640px
        1200: {
          slidesPerView: 3,
        },
        1600: {
          slidesPerView: 4,
        },
      },
    }
  );

  /*-----------------------------------
  # blog carousel
  ------------------------------ */

  var blogCarousel = new Swiper(".blog-carousel .swiper-container", {
    slidesPerView: 4,
    spaceBetween: 30,
    pagination: false,
    navigation: {
      nextEl: ".blog-carousel .swiper-button-next",
      prevEl: ".blog-carousel .swiper-button-prev",
    },
    observer: true,
    observeParents: true,
    // Responsive breakpoints
    breakpoints: {
      // when window width is >= 320px
      0: {
        slidesPerView: 1,
        loop: true,
        autoplay: {
          delay: 2000,
        },
        speed: 1000,
      },
      // when window width is >= 480px
      768: {
        slidesPerView: 2,
      },
      992: {
        slidesPerView: 3,
      },
    },
  });
  /*-----------------------------------
  # product list carousel
  ------------------------------ */

  var bestsellerCarousel = new Swiper(
    ".bestseller-carousel .swiper-container",
    {
      slidesPerView: 3,
      spaceBetween: 30,
      pagination: false,
      navigation: {
        nextEl: ".bestseller-carousel .swiper-button-next",
        prevEl: ".bestseller-carousel .swiper-button-prev",
      },
      observer: true,
      observeParents: true,
      // Responsive breakpoints
      breakpoints: {
        // when window width is >= 320px
        0: {
          slidesPerView: 1,
          loop: true,
          autoplay: {
            delay: 2000,
          },
          speed: 1000,
        },
        // when window width is >= 480px
        768: {
          slidesPerView: 2,
        },
        1200: {
          slidesPerView: 3,
        },
      },
    }
  );

  // swiper thumb gallery

  var galleryThumbs = new Swiper(
    ".product-modal-gallery-thumbs .swiper-container",
    {
      spaceBetween: 0,
      slidesPerView: 4,
      loop: false,
      watchSlidesVisibility: true,
      watchSlidesProgress: true,
      // If we need pagination
      pagination: {
        el: ".product-modal-gallery-thumbs .swiper-pagination",
        clickable: true,
      },
    }
  );

  var galleryTop = new Swiper(".product-modal-gallery .swiper-container", {
    spaceBetween: 1,
    spaceBetween: 0,
    loop: false,
    navigation: {
      nextEl: ".product-modal-gallery .swiper-button-next",
      prevEl: ".product-modal-gallery .swiper-button-prev",
    },
    thumbs: {
      swiper: galleryThumbs,
    },
  });
  // swiper thumb gallery

  var Thumbs = new Swiper(".gallery-thumbs .swiper-container", {
    spaceBetween: 0,
    slidesPerView: 4,
    loop: false,
    watchSlidesVisibility: true,
    watchSlidesProgress: true,
  });

  var galleryTop2 = new Swiper(".gallery .swiper-container", {
    spaceBetween: 1,
    spaceBetween: 0,
    loop: false,
    navigation: {
      nextEl: ".gallery .swiper-button-next",
      prevEl: ".gallery .swiper-button-prev",
    },
    thumbs: {
      swiper: Thumbs,
    },
  });

  /*--------------------------     
# counter 
-------------------------- */

  $(".count").each(function () {
    var count = $(this),
      input = count.find('input[type="number"]'),
      increament = count.find(".increment"),
      decreament = count.find(".decrement"),
      minValue = input.attr("min"),
      maxValue = input.attr("max");
    increament.on("click", function () {
      var oldValue = parseFloat(input.val());

      if (oldValue >= maxValue) {
        var newVal = oldValue;
      } else {
        var newVal = oldValue + 1;
      }

      count.find("input").val(newVal);
      count.find("input").trigger("change");
    });
    decreament.on("click", function () {
      var oldValue = parseFloat(input.val());

      if (oldValue <= minValue) {
        var newVal = oldValue;
      } else {
        var newVal = oldValue - 1;
      }

      count.find("input").val(newVal);
      count.find("input").trigger("change");
    });
  });

  // slider-range
  $("#slider-range").slider({
    range: true,
    min: 0,
    max: 100,
    values: [0, 100],
    slide: (event, ui) => {
      $("#amount").val("$" + ui.values[0] + " - $" + ui.values[1]);
    },
  });

  $("#amount").val(
    "$" +
      $("#slider-range").slider("values", 0) +
      " - $" +
      $("#slider-range").slider("values", 1)
  );
  /*-------------------------
    Create an account toggle
    --------------------------*/

  $(".checkout-toggle2").on("click", function () {
    $(".open-toggle2").slideToggle(1000);
  });
  $(".checkout-toggle").on("click", function () {
    $(".open-toggle").slideToggle(1000);
  });

  /*
     Progress
      ================================== */
  var Element = $(".skill-bar");
  Element.each(function () {
    var $element = $(this);
    $element.waypoint(
      function () {
        var progressBar = $(".progress-bar");
        progressBar.each(function () {
          var eachBarWidth = $(this).attr("aria-valuenow");
          $(this).width(eachBarWidth + "%");
        });
      },
      {
        offset: $element.data("ht-offset"),
      }
    );
  });

  /*----------------------------
  # Mail Chip Ajax active
  ------------------------------*/
  var mCForm = $("#mc-form");
  mCForm.ajaxChimp({
    callback: mailchimpCallback,
    //Replace this with your own mailchimp post URL. Don't remove the "". Just paste the url inside "".
    url: "http://devitems.us11.list-manage.com/subscribe/post?u=6bbb9b6f5827bd842d9640c82&id=05d85f18ef",
  });
  function mailchimpCallback(resp) {
    if (resp.result === "success") {
      alert(resp.msg);
    } else if (resp.result === "error") {
      alert(resp.msg);
    }
    return false;
  }

  /*----------------------------
  #  Copy Right Year Update
  -------------------------------*/

  $("#currentYear").text(new Date().getFullYear());
  /*----------------------------
  #  scrollUp
  -------------------------------*/
  $.scrollUp({
    scrollName: "scrollUp",
    // Element ID
    scrollDistance: 200,
    // Distance from top/bottom before showing element (px)
    scrollFrom: "top",
    // 'top' or 'bottom'
    scrollSpeed: 400,
    // Speed back to top (ms)
    easingType: "linear",
    // Scroll to top easing (see http://easings.net/)
    animation: "fade",
    // Fade, slide, none
    animationSpeed: 200,
    // Animation speed (ms)
    scrollTrigger: false,
    // Set a custom triggering element. Can be an HTML string or jQuery object
    scrollTarget: false,
    // Set a custom target element for scrolling to. Can be element or number
    scrollText: '<i class="ion-android-arrow-up"></i>',
    // Text for element, can contain HTML
    scrollTitle: false,
    // Set a custom <a> title if required.
    scrollImg: false,
    // Set true to use image
    activeOverlay: false,
    // Set CSS color to display scrollUp active point, e.g '#00FFFF'
    zIndex: 214, // Z-Index for the overlay
  });
})(jQuery);
