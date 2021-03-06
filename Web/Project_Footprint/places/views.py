from django.shortcuts import render
from .models import Place
from django.http import request
from bs4 import BeautifulSoup
import json
import requests
from .db import add_to_db

# from multiprocessing import Pool, Manager  ??
def index(request):
    """
     메인화면 페이지
     Hotplace 정보를 6개 전달하여 보여줌
    """
    sights = Place.objects.filter(place_div=0).order_by('-count')[:6]
    restaurants = Place.objects.filter(place_div=1).order_by('-count')[:6]
    user = request.user
    return render(request, 'index.html', {'sights': sights, 'restaurants': restaurants, 'user': user})

def place_detail(request, id):
    """
    장소 자세히보기
    크롤링한 데이터를 기반으로 한 장소 자세히보기 페이지 보여줌
    """
    context = {
        'places': place_detail_crawl(pk=id)
    }
    return render(request, 'place_detail.html', context)

def place_detail_crawl(pk):
    """
     네이버 플레이스 페이지를 크롤링
    """
    URL = 'https://store.naver.com/restaurants/detail?id'
    naverPlaceID = pk
    result = requests.get(f'{URL}={pk}')
    soup = BeautifulSoup(result.content, 'html.parser')

    title = soup.find("strong", {"class": "name"})
    title = str(title.string).strip()

    category = soup.find("span", {"class": "category"})
    category = str(category.string).strip()

    location = soup.find("span", {"class": "addr"})
    location = str(location.string).strip()

    businessHours = soup.find("span", {"class": "time"})
    if businessHours is not None:
        if businessHours is soup.find("span", {"class": "highlight"}):
            businessHours = str(businessHours.string).strip()
        else:
            businessHours = " "
    else:
        businessHours = " "

    desc = soup.find("div", {"class": "info"})
    description = desc.find("span", {"class": "txt"})
    if description is not None:
        tag = soup.find("span", {"class": "kwd"})
        if tag is not None:
            description = " "
        else:
            description = str(description.string).strip()
    else:
        description = " "

    URL_IMG = 'https://store.naver.com/restaurants/detail?id'
    result_IMG = requests.get(f'{URL_IMG}={pk}&tab=photo')

    soups = BeautifulSoup(result_IMG.content, 'html.parser')
    area = soups.find("div", {"class": "list_photo"})
    a = area.find("a")
    if a is not None:
        imageSrc = a.find("img").get("src")
    else:
        a = area.find("div")
        imageSrc = a.find("img").get("src")

    menuName = []
    list_menu = soup.find("ul", {"class": "list_menu"})
    if list_menu is not None:
        menu = list_menu.find_all("span", {"class": "name"})
        for item in menu:
            menuName.append(item.get_text())
        menuNames = menuName
        menuName=json.dumps(menuName,ensure_ascii=False)
    else:
        menuName = []
        menuNames = ""
    price = soup.find_all("em", {"class": "price"})

    menuPrice = []
    if price is not None:
        for item in price:
            menuPrice.append(item.get_text())
        menuPrices = menuPrice
        menuPrice = json.dumps(menuPrice,ensure_ascii=False)
    else:
        menuPrice = []
        menuPrices = ""

    res = {
        'naverPlaceID': naverPlaceID,
        'title': title,
        'category': category,
        'location': location,
        'businessHours': businessHours,
        'description': description,
        'imageSrc': imageSrc,
        'menuName': menuName,
        'menuNames': menuNames,
        'menuPrices': menuPrices,
        'menuPrice': menuPrice,
    }
    add_to_db(res)

    return res

def get_hotplace():
    """
     현재 Hotplace인 장소 5개 NaverPlaceID 정보를 제공함
    """
    hotplaces = Place.objects.order_by('-count')[:5]
    res = []
    for item in hotplaces:
        res.append(item.naver_place_id)
        place_detail_crawl(item.naver_place_id)
    return res